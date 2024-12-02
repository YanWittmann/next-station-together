// get the current url host/port as the apiServer
const apiServer = window.location.origin;
console.log('apiServer:', apiServer);

let activeBoardId = null;
const canvas = document.getElementById('gameCanvas');
let activeBoard = null;

function initializeGame() {
    document.getElementById('board-id').addEventListener('input', (e) => {
        activeBoardId = e.target.value;
        updateBoard();
    });

    if (document.getElementById('board-id').value) {
        activeBoardId = document.getElementById('board-id').value;
        updateBoard();
    }

    function updateBoard() {
        if (activeBoardId) {
            if (activeBoard) {
                activeBoard.destroyBoard();
            }
            activeBoard = new GameBoard(canvas);
        }
    }
}

class GameBoard {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.STATION_SIZE = 75;
        this.prebakedConnections = [];
        this.userConnections = [];
        this.dragStart = null;
        this.currentConnection = null;
        this.boardData = null;
        this.iconCache = new Map();
        this.selectedColor = 'rgb(255, 145, 35)'; // Default color

        this.setupCanvas();
        this.setupEventListeners();
        this.loadBoardData();
    }

    setupCanvas() {
        // Set canvas size based on container
        const container = this.canvas.parentElement;
        const size = container.clientWidth;
        this.canvas.width = size;
        this.canvas.height = size;

        // Scale everything based on container size
        this.scale = size / (this.STATION_SIZE * 10);
        this.ctx.scale(this.scale, this.scale);
    }

    setupEventListeners() {
        this.canvas.addEventListener('mousedown', this.handleMouseDown.bind(this));
        this.canvas.addEventListener('mousemove', this.handleMouseMove.bind(this));
        this.canvas.addEventListener('mouseup', this.handleMouseUp.bind(this));
        this.canvas.addEventListener('contextmenu', (e) => {
            e.preventDefault();
            this.handleRightClick(e);
        });

        // Color selector event listeners
        const colorButtons = document.querySelectorAll('.color-btn');
        colorButtons.forEach(button => {
            button.addEventListener('click', () => {
                this.selectedColor = button.dataset.color;
                colorButtons.forEach(btn => btn.classList.remove('selected'));
                button.classList.add('selected');
            });
        });

        // Set initial selected color
        colorButtons[0].classList.add('selected');
    }

    async loadBoardData() {
        try {
            const response = await fetch(`${apiServer}/boards/${activeBoardId}/board-data.json`);
            this.boardData = await response.json();
            this.prebakedConnections = this.boardData.connections.map(conn => ({
                x1: conn.x1,
                y1: conn.y1,
                x2: conn.x2,
                y2: conn.y2,
                color: '#ededed'
            }));
            await this.preloadImages();
            this.draw();
            this.populateScoreTable();
            console.log('Board data loaded:', this.boardData);
        } catch (error) {
            console.error('Error loading board data:', error);
        }
    }

    async preloadImages() {
        const loadImage = (texture) => {
            return new Promise((resolve) => {
                const img = new Image();
                img.crossOrigin = "anonymous";
                img.onload = () => {
                    this.iconCache.set(texture, img);
                    resolve();
                };
                img.src = `${apiServer}/boards/${activeBoardId}/img/${texture}.png`;
            });
        };

        const textures = new Set();
        this.boardData.stations.forEach(station => textures.add(station.texture));
        this.boardData.intersections.forEach(intersection => textures.add(intersection.texture));

        await Promise.all([...textures].map(loadImage));
    }

    draw() {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        this.drawRiver();

        this.drawDistricts();

        this.drawPrebakedConnections();

        this.drawUserConnections();

        this.drawIntersections();

        this.drawStations();

        if (this.dragStart && this.currentConnection) {
            this.drawConnection(
                this.dragStart.x,
                this.dragStart.y,
                this.currentConnection.x,
                this.currentConnection.y,
                this.selectedColor
            );
        }
    }

    drawRiver() {
        const path = this.boardData.riverLayout.path;
        this.ctx.beginPath();
        this.ctx.strokeStyle = getComputedStyle(document.documentElement)
            .getPropertyValue('--river-color').trim();
        this.ctx.lineWidth = 16;
        this.ctx.lineCap = 'round';
        this.ctx.lineJoin = 'round';

        for (let i = 0; i < path.length - 1; i++) {
            const point1 = path[i];
            const point2 = path[i + 1];
            const x1 = point1.x * this.STATION_SIZE - this.STATION_SIZE / 2;
            const y1 = point1.y * this.STATION_SIZE - this.STATION_SIZE / 2;
            const x2 = point2.x * this.STATION_SIZE - this.STATION_SIZE / 2;
            const y2 = point2.y * this.STATION_SIZE - this.STATION_SIZE / 2;

            if (i === 0) {
                this.ctx.moveTo(x1, y1);
            }
            this.ctx.lineTo(x2, y2);
        }
        this.ctx.stroke();
    }

    drawDistricts() {
        this.ctx.strokeStyle = getComputedStyle(document.documentElement)
            .getPropertyValue('--district-color').trim();
        this.ctx.lineWidth = 3;

        this.boardData.districts.forEach(district => {
            const x = district.x * this.STATION_SIZE;
            const y = district.y * this.STATION_SIZE;
            const width = district.width * this.STATION_SIZE;
            const height = district.height * this.STATION_SIZE;

            this.ctx.strokeRect(x, y, width, height);
        });
    }

    drawStations() {
        this.boardData.stations.forEach(station => {
            const x = station.x * this.STATION_SIZE;
            const y = station.y * this.STATION_SIZE;
            const icon = this.iconCache.get(station.texture);

            if (icon) {
                this.ctx.drawImage(
                    icon,
                    x + 5,
                    y + 5,
                    this.STATION_SIZE - 10,
                    this.STATION_SIZE - 10
                );
            }
        });
    }

    drawPrebakedConnections() {
        this.ctx.setLineDash([9]);
        this.ctx.lineWidth = 2;
        this.prebakedConnections.forEach(conn => {
            this.drawConnection(conn.x1, conn.y1, conn.x2, conn.y2, conn.color);
        });
        this.ctx.setLineDash([]);
    }

    drawUserConnections() {
        this.ctx.setLineDash([]);
        this.ctx.lineWidth = 7;
        this.userConnections.forEach(conn => {
            this.drawConnection(conn.x1, conn.y1, conn.x2, conn.y2, conn.color);
        });
    }

    drawConnection(x1, y1, x2, y2, color) {
        this.ctx.beginPath();
        this.ctx.strokeStyle = color;
        this.ctx.moveTo(
            x1 * this.STATION_SIZE + this.STATION_SIZE / 2,
            y1 * this.STATION_SIZE + this.STATION_SIZE / 2
        );
        this.ctx.lineTo(
            x2 * this.STATION_SIZE + this.STATION_SIZE / 2,
            y2 * this.STATION_SIZE + this.STATION_SIZE / 2
        );
        this.ctx.stroke();
    }

    drawIntersections() {
        const intersectionSize = this.STATION_SIZE * 3 / 5;

        this.boardData.intersections.forEach(intersection => {
            const x = intersection.x * this.STATION_SIZE + (this.STATION_SIZE - intersectionSize) / 2;
            const y = intersection.y * this.STATION_SIZE + (this.STATION_SIZE - intersectionSize) / 2;
            const icon = this.iconCache.get(intersection.texture);

            if (icon) {
                this.ctx.drawImage(icon, x, y, intersectionSize, intersectionSize);
            }
        });
    }

    getConnectionColor(startingPosition) {
        const colors = [
            'var(--connection-color-0)',
            'var(--connection-color-1)',
            'var(--connection-color-2)',
            'var(--connection-color-3)'
        ];
        return colors[startingPosition] || '#ededed';
    }

    getStationAt(x, y) {
        const gridX = Math.floor(x / (this.STATION_SIZE * this.scale));
        const gridY = Math.floor(y / (this.STATION_SIZE * this.scale));

        return this.boardData.stations.find(
            station => station.x === gridX && station.y === gridY
        );
    }

    handleMouseDown(e) {
        const rect = this.canvas.getBoundingClientRect();
        const mouseX = (e.clientX - rect.left) / this.scale;
        const mouseY = (e.clientY - rect.top) / this.scale;

        const gridX = Math.floor(mouseX / this.STATION_SIZE);
        const gridY = Math.floor(mouseY / this.STATION_SIZE);

        const station = this.boardData.stations.find(
            station => station.x === gridX && station.y === gridY
        );

        if (station) {
            this.dragStart = {
                x: gridX,
                y: gridY,
                station: station
            };
            this.currentConnection = {
                x: gridX,
                y: gridY
            };
        }
    }

    handleMouseMove(e) {
        if (!this.dragStart) return;

        const rect = this.canvas.getBoundingClientRect();
        const mouseX = (e.clientX - rect.left) / this.scale;
        const mouseY = (e.clientY - rect.top) / this.scale;

        const gridX = Math.floor(mouseX / this.STATION_SIZE);
        const gridY = Math.floor(mouseY / this.STATION_SIZE);

        // Only update if we're over a valid station
        const targetStation = this.boardData.stations.find(
            station => station.x === gridX && station.y === gridY
        );

        if (targetStation) {
            this.currentConnection = { x: gridX, y: gridY };
        }

        this.draw();
    }

    handleMouseUp(e) {
        if (!this.dragStart || !this.currentConnection) {
            this.dragStart = null;
            this.currentConnection = null;
            return;
        }

        const rect = this.canvas.getBoundingClientRect();
        const mouseX = (e.clientX - rect.left) / this.scale;
        const mouseY = (e.clientY - rect.top) / this.scale;

        const gridX = Math.floor(mouseX / this.STATION_SIZE);
        const gridY = Math.floor(mouseY / this.STATION_SIZE);

        const endStation = this.boardData.stations.find(
            station => station.x === gridX && station.y === gridY
        );

        if (endStation && endStation !== this.dragStart.station) {
            this.userConnections.push({
                x1: this.dragStart.x,
                y1: this.dragStart.y,
                x2: endStation.x,
                y2: endStation.y,
                color: this.selectedColor
            });
        }

        this.dragStart = null;
        this.currentConnection = null;
        this.draw();
    }

    handleRightClick(e) {
        const rect = this.canvas.getBoundingClientRect();
        const x = (e.clientX - rect.left) / this.scale;
        const y = (e.clientY - rect.top) / this.scale;

        const station = this.getStationAt(x, y);
        if (station) {
            this.userConnections = this.userConnections.filter(conn =>
                !(conn.x1 === station.x && conn.y1 === station.y) &&
                !(conn.x2 === station.x && conn.y2 === station.y)
            );
            this.draw();
        }
    }

    populateScoreTable() {
        const scoreTable = document.querySelector('.score-table');
        scoreTable.innerHTML = ''; // Clear existing content

        // Turn-wise score contributors
        const turnWiseContributors = [
            this.boardData.turnWiseScoreContributorA,
            this.boardData.turnWiseScoreContributorB,
            this.boardData.turnWiseScoreContributorC
        ];

        // End-game score contributors
        const endGameContributors = [
            this.boardData.endGameScoreContributorA,
            this.boardData.endGameScoreContributorB,
            this.boardData.endGameScoreContributorC
        ];

        // Create a 12x8 array to represent the table
        const tableData = Array(10).fill().map(() => Array(16).fill(''));

        // Populate the array with cell contents
        function createTurnWiseContributorRow(rowIndex, contributorIndex, symbol) {
            tableData[rowIndex][0] = `<td class="regular-height"><img src="${apiServer}/boards/${activeBoardId}/img/${turnWiseContributors[contributorIndex].texture}.png" alt="${turnWiseContributors[contributorIndex].type}" class="score-icon"></td>`;
            tableData[rowIndex][1] = '<td class="regular-height"><input type="number" class="score-input"></td>';
            tableData[rowIndex][2] = '<td class="regular-height"></td>';
            tableData[rowIndex][3] = '<td class="regular-height"><input type="number" class="score-input"></td>';
            tableData[rowIndex][4] = '<td class="regular-height"></td>';
            tableData[rowIndex][5] = '<td class="regular-height"><input type="number" class="score-input"></td>';
            tableData[rowIndex][6] = '<td class="regular-height"></td>';
            tableData[rowIndex][7] = '<td class="regular-height"><input type="number" class="score-input"></td>';
            tableData[rowIndex][8] = '<td class="regular-height score-table-turnwise-border"><div/></td>';

            tableData[rowIndex + 1][1] = `<td class="score-table-operation-symbol">${symbol}</td>`;
            tableData[rowIndex + 1][3] = `<td class="score-table-operation-symbol">${symbol}</td>`;
            tableData[rowIndex + 1][5] = `<td class="score-table-operation-symbol">${symbol}</td`;
            tableData[rowIndex + 1][7] = `<td class="score-table-operation-symbol">${symbol}</td>`;
            tableData[rowIndex + 1][8] = `<td class="score-table-turnwise-border"><div/></td>`;
        }

        createTurnWiseContributorRow(0, 0, "×");
        createTurnWiseContributorRow(2, 1, "+");
        createTurnWiseContributorRow(4, 2, "=");

        // sum cells
        tableData[6][1] = '<td class="regular-height"><input type="number" class="score-input input-turnwise"></td>';
        tableData[6][2] = '<td class="regular-height width-none score-table-operation-symbol">+</td>';
        tableData[6][3] = '<td class="regular-height"><input type="number" class="score-input input-turnwise"></td>';
        tableData[6][4] = '<td class="regular-height width-none score-table-operation-symbol">+</td>';
        tableData[6][5] = '<td class="regular-height"><input type="number" class="score-input input-turnwise"></td>';
        tableData[6][6] = '<td class="regular-height width-none score-table-operation-symbol">+</td>';
        tableData[6][7] = '<td class="regular-height"><input type="number" class="score-input input-turnwise"></td>';
        tableData[6][8] = '<td class="regular-height width-none score-table-operation-symbol">=</td>';
        tableData[6][9] = '<td class="regular-height"><input type="number" class="score-input input-turnwise" style="max-width:60px;"></td>';

        function createEndGameContributorRow(rowIndex, contributorIndex) {
            tableData[rowIndex][9] = `<td class="regular-height"><img src="${apiServer}/boards/${activeBoardId}/img/${endGameContributors[contributorIndex].texture}.png" alt="${endGameContributors[contributorIndex].type}" class="score-icon"></td>`;
            tableData[rowIndex][10] = '<td class="regular-height score-table-operation-symbol">×</td>';
            tableData[rowIndex][11] = '<td class="regular-height"><input type="number" class="score-input"></td>';
            tableData[rowIndex][12] = '<td class="regular-height score-table-operation-symbol">=</td>';
            tableData[rowIndex][13] = '<td class="regular-height"><input type="number" class="score-input"></td>';
            tableData[rowIndex + 1][13] = '<td class="score-table-operation-symbol">+</td>';
            tableData[rowIndex][14] = `<td class="score-table-endgame-border"></td>`;
            tableData[rowIndex + 1][14] = `<td class="score-table-endgame-border"></td>`;
        }

        createEndGameContributorRow(0, 0);
        createEndGameContributorRow(2, 1);
        createEndGameContributorRow(4, 2);
        tableData[5][13] = '<td class="regular-height score-table-operation-symbol">=</td>';

        // bonus points
        tableData[0][15] = `<td class="regular-height"><img src="${apiServer}/static/img/common-goal-icon.png" alt="Common Goal" class="score-icon"></td>`;
        tableData[1][15] = `<td class=""><span class="regular-height score-table-operation-symbol" style="font-size: 24px;">⇓</span></td>`;
        // checkbox
        tableData[2][15] = `<td class="regular-height" style="background: #4e8cff;"><input type="checkbox" class="score-input"></td>`;
        tableData[3][15] = `<td class="regular-height width-none" style="vertical-align: top !important; position: relative;"><div class="common-goal-text">+10</div></td>`;
        tableData[4][15] = `<td class="regular-height" style="background: #4e8cff;"><input type="checkbox" class="score-input"></td>`;
        tableData[5][15] = `<td class="regular-height width-none" style="vertical-align: top !important; position: relative;"><div class="common-goal-text">+10</div></td>`;

        // add row below
        tableData[6][10] = '<td class="regular-height score-table-operation-symbol">+</td>';
        tableData[6][11] = '<td class="regular-height"><input type="number" class="score-input circular-input"></td>';
        tableData[6][12] = '<td class="regular-height score-table-operation-symbol">+</td>';
        tableData[6][13] = '<td class="regular-height"><input type="number" class="score-input input-endgame"></td>';
        tableData[6][14] = '<td class="regular-height score-table-operation-symbol">+</td>';
        tableData[6][15] = '<td class="regular-height"><input type="number" class="score-input input-common-goal"></td>';

        // below that, the total sum
        tableData[7][13] = '<td class="regular-height" colspan="3" style="padding-top:5px;"><input type="number" class="score-input input-total" style="font-size: 24px;"></td>';

        if (this.boardData.progressScoreContributor) {
            const progressScoreContributor = this.boardData.progressScoreContributor;
            if (progressScoreContributor.type === "ProgressScoreCompoundContributor") {
                const contributorA = progressScoreContributor.scoreContributorA;
                const contributorB = progressScoreContributor.scoreContributorB;
                const contributorC = progressScoreContributor.scoreContributorC;

                // add an input and the icon for each from right to left starting below the circular input
                if (contributorA?.type) {
                    tableData[7][11] = `<td class="regular-height"><input type="number" class="score-input"></td>`;
                    tableData[7][10] = `<td class="regular-height score-table-operation-symbol">×</td>`;
                    tableData[7][9] = `<td class="regular-height"><img src="${apiServer}/boards/${activeBoardId}/img/${contributorA.texture}.png" alt="${contributorA.type}" class="score-icon"></td>`;
                }
                if (contributorB?.type) {
                    tableData[7][8] = `<td class="regular-height score-table-operation-symbol">+</td>`;
                    tableData[7][7] = `<td class="regular-height"><input type="number" class="score-input"></td>`;
                    tableData[7][6] = `<td class="regular-height score-table-operation-symbol">×</td>`;
                    tableData[7][5] = `<td class="regular-height"  style="max-width:70px;width:70px;"><img src="${apiServer}/boards/${activeBoardId}/img/${contributorB.texture}.png" alt="${contributorB.type}" class="score-icon"></td>`;
                }
                if (contributorC?.type) {
                    tableData[7][4] = `<td class="regular-height score-table-operation-symbol">+</td>`;
                    tableData[7][3] = `<td class="regular-height"><input type="number" class="score-input"></td>`;
                    tableData[7][2] = `<td class="regular-height score-table-operation-symbol">×</td>`;
                    tableData[7][1] = `<td class="regular-height"><img src="${apiServer}/boards/${activeBoardId}/img/${contributorC.texture}.png" alt="${contributorC.type}" class="score-icon"></td>`;
                }

            } else if (progressScoreContributor.type === "ProgressScoreMonuments") {
                // create a single cell (colspan count) and subdivide manually using flex such that all have the same width, there are count icons in the contributor this time
                const count = progressScoreContributor.textures.length;
                tableData[7][0] = `<td class="regular-height" colspan="${count}" style="padding: 0;"><div class="monument-container">`;
                for (let i = 0; i < count; i++) {
                    tableData[7][0] += `<div class="monument-icon-container"><img src="${apiServer}/boards/${activeBoardId}/img/${progressScoreContributor.textures[i]}.png" alt="Monument" class="score-icon"></div>`;
                }
                tableData[7][0] += '</div></td>';
            }
        }


        // Construct the table using the array
        for (let i = 0; i < tableData.length; i++) {
            const row = scoreTable.insertRow();
            row.dataset.rowIndex = "" + i;
            for (let j = 0; j < tableData[i].length; j++) {
                if (tableData[i][j]) {
                    row.innerHTML += tableData[i][j];
                    // check if there is a colspan, skip this many cells
                    if (tableData[i][j].includes('colspan')) {
                        const colspan = parseInt(tableData[i][j].match(/colspan="(\d+)"/)[1]);
                        j += colspan - 1;
                    }
                } else {
                    row.insertCell();
                }
            }
        }

        // turnwise score calculation
        connectInputs(1, 6, [{ x: 1, y: 0 }, { x: 1, y: 2 }, { x: 1, y: 4 }], ([a, b, c]) => a * b + c);
        connectInputs(3, 6, [{ x: 3, y: 0 }, { x: 3, y: 2 }, { x: 3, y: 4 }], ([a, b, c]) => a * b + c);
        connectInputs(5, 6, [{ x: 5, y: 0 }, { x: 5, y: 2 }, { x: 5, y: 4 }], ([a, b, c]) => a * b + c);
        connectInputs(7, 6, [{ x: 7, y: 0 }, { x: 7, y: 2 }, { x: 7, y: 4 }], ([a, b, c]) => a * b + c);

        // endgame score calculation
        connectInputs(13, 0, [{ x: 11, y: 0 }], ([a]) => a * this.boardData.endGameScoreContributorA.multiplier);
        connectInputs(13, 2, [{ x: 11, y: 2 }], ([a]) => a * this.boardData.endGameScoreContributorB.multiplier);
        connectInputs(13, 4, [{ x: 11, y: 4 }], ([a]) => a * this.boardData.endGameScoreContributorC.multiplier);

        // other goal calculation
        if (this.boardData.progressScoreContributor) {
            const progressScoreContributor = this.boardData.progressScoreContributor;
            if (progressScoreContributor.type === "ProgressScoreCompoundContributor") {
                const contributorA = progressScoreContributor.scoreContributorA;
                const contributorB = progressScoreContributor.scoreContributorB;
                const contributorC = progressScoreContributor.scoreContributorC;

                const dependants = [];
                if (contributorA?.type) dependants.push({ x: 11, y: 7 });
                if (contributorB?.type) dependants.push({ x: 7, y: 7 });
                if (contributorC?.type) dependants.push({ x: 3, y: 7 });

                connectInputs(11, 6, dependants, values => {
                    let result = 0;
                    if (contributorA?.type) result += values[0] * contributorA.multiplier;
                    if (contributorB?.type) result += values[1] * contributorB.multiplier;
                    if (contributorC?.type) result += values[2] * contributorC.multiplier;
                    return result;
                });

            } else if (progressScoreContributor.type === "ProgressScoreMonuments") {
                const monumentContainers = document.querySelectorAll('.monument-icon-container');
                // add click listener to each monument icon, on click it should toggle a data attribute "selected" and should apply a backdrop shadow effect and calculate the total score
                monumentContainers.forEach((container, index) => {
                    const input11_6 = getInputElement(11, 6);
                    input11_6.value = 0;
                    container.addEventListener('click', () => {
                        const selected = container.dataset.selected === 'true';
                        container.dataset.selected = !selected;
                        container.style.filter = selected ? 'none' : 'drop-shadow(0 0 3px red)';
                        const selectedMonuments = Array.from(monumentContainers).filter(c => c.dataset.selected === 'true').length;
                        input11_6.value = progressScoreContributor.fields[selectedMonuments] || 0;
                    });
                });
            }
        }


        // common goal score calculation
        connectInputs(15, 6, [{ x: 15, y: 2 }, { x: 15, y: 4 }], ([a, b]) => a * 10 + b * 10);

        // total score calculation
        connectInputs(9, 6, [{ x: 1, y: 6 }, { x: 3, y: 6 }, { x: 5, y: 6 }, {
            x: 7,
            y: 6
        }], ([a, b, c, d]) => a + b + c + d);
        connectInputs(13, 6, [{ x: 13, y: 0 }, { x: 13, y: 2 }, { x: 13, y: 4 }], ([a, b, c]) => a + b + c);
        connectInputs(13, 7, [{ x: 9, y: 6 }, { x: 11, y: 6 }, { x: 13, y: 6 }, {
            x: 15,
            y: 6
        }], ([a, b, c, d]) => a + b + c + d);

        // Utility to fetch an input element at a specific position
        function getInputElement(x, y) {
            const row = scoreTable.rows[y];
            if (!row) return null;
            let cellIndex = 0;
            for (let i = 0; i < row.cells.length; i++) {
                const cell = row.cells[i];
                const colspan = cell.colSpan || 1;
                if (cellIndex === x) {
                    return cell.querySelector('input');
                }
                cellIndex += colspan;
            }
            return null;
        };


        // debug add click event listener to all input fields and print their coordinates
        const inputs = document.querySelectorAll('.score-input');
        inputs.forEach(input => {
            input.addEventListener('click', () => {
                const cell = input.closest('td');
                const row = cell.parentElement;
                const x = cell.cellIndex;
                const y = row.rowIndex;
                console.log('Clicked:', x, y);
            });
        });

        function connectInputs(x, y, inputs, calculate) {
            const scoreTable = document.querySelector('.score-table');

            // Fetch the target element
            const targetInput = getInputElement(x, y);
            if (!targetInput) {
                console.error('Target input not found:', x, y);
                return;
            }

            targetInput.value = '0';

            // Attach event listeners to all dependent inputs
            inputs.forEach(({ x, y }) => {
                const input = getInputElement(x, y);
                if (input) {
                    input.addEventListener('input', () => {
                        const values = inputs.map(({ x, y }) => {
                            const depInput = getInputElement(x, y);
                            if (!depInput) {
                                console.error('Dependent input not found:', x, y);
                                return 0;
                            }
                            // check type
                            if (depInput.type === 'checkbox') {
                                return depInput.checked ? 1 : 0;
                            }
                            return depInput.value ? parseFloat(depInput.value) : 0;
                        });
                        targetInput.value = calculate(values).toFixed(2).replace(/\.0+$/, '');
                        targetInput.dispatchEvent(new Event('input', { bubbles: true }));
                    });
                } else {
                    console.error('Input not found:', x, y);
                    targetInput.style.backgroundColor = 'red';
                }
            });
        }
    }

    destroyBoard() {
        // listeners
        this.canvas.removeEventListener('mousedown', this.handleMouseDown);
        this.canvas.removeEventListener('mousemove', this.handleMouseMove);
        this.canvas.removeEventListener('mouseup', this.handleMouseUp);
        this.canvas.removeEventListener('contextmenu', this.handleRightClick);
        // clear canvas
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        // clear score table
        const scoreTable = document.querySelector('.score-table');
        scoreTable.innerHTML = '';
    }
}

initializeGame();
