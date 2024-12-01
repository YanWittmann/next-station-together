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
            const response = await fetch('http://localhost:8000/board-data.json');
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
                img.src = `http://localhost:8000/img/${texture}.png`;
            });
        };

        const textures = new Set();
        this.boardData.stations.forEach(station => textures.add(station.texture));
        this.boardData.intersections.forEach(intersection => textures.add(intersection.texture));

        await Promise.all([...textures].map(loadImage));
    }

    draw() {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        // Draw river
        this.drawRiver();

        // Draw districts
        this.drawDistricts();

        // Draw connections
        this.drawPrebakedConnections();
        this.drawUserConnections();

        // Draw stations
        this.drawStations();

        // Draw intersections
        this.drawIntersections();

        // Draw current dragging connection if any
        if (this.dragStart && this.currentConnection) {
            this.drawConnection(
                this.dragStart.x,
                this.dragStart.y,
                this.currentConnection.x,
                this.currentConnection.y,
                this.getConnectionColor(this.dragStart.station)
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
        this.ctx.lineWidth = 3;
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

    getConnectionColor(station) {
        const colors = [
            'var(--connection-color-0)',
            'var(--connection-color-1)',
            'var(--connection-color-2)',
            'var(--connection-color-3)'
        ];
        return colors[station.startingPosition] || '#ededed';
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
        const tableData = Array(10).fill().map(() => Array(15).fill(''));

        // Populate the array with cell contents
        /*for (let i = 0; i < 8; i++) {
            if (i < 3) {
                const contributor = turnWiseContributors[i];
                tableData[i][0] = `<td><img src="http://localhost:8000/img/${contributor.texture}.png" alt="${contributor.type}" class="score-icon"></td>`;
                // tableData[i][1] = `<td>${i === 2 ? '×' + contributor.multiplier : (i === 1 ? '+' : '×')}</td>`;
                for (let j = 1; j < 5; j++) {
                    tableData[i][j] = '<td><input type="number" class="score-input"></td>';
                }
            } else if (i === 3) {
                tableData[i][0] = '<td colspan="2">=</td>';
                for (let j = 1; j < 5; j++) {
                    tableData[i][j] = '<td class="sum-cell"></td>';
                }
            }

            tableData[i][6] = '<td class="separator"></td>';

            if (i < 3) {
                const contributor = endGameContributors[i];
                tableData[i][7] = `<td><img src="http://localhost:8000/img/${contributor.texture}.png" alt="${contributor.type}" class="score-icon"></td>`;
                tableData[i][8] = `<td>×${contributor.multiplier}</td>`;
                tableData[i][9] = '<td><input type="number" class="score-input"></td>';
            } else if (i === 3) {
                tableData[i][7] = '<td colspan="2">=</td>';
                tableData[i][9] = '<td class="sum-cell"></td>';
            }

            if (i === 0) {
                tableData[i][10] = `
                    <td rowspan="3" class="bonus-section">
                        <div class="bonus-icons">
                            <img src="placeholder1.png" alt="Bonus 1" class="score-icon">
                            <img src="placeholder2.png" alt="Bonus 2" class="score-icon">
                            <img src="placeholder3.png" alt="Bonus 3" class="score-icon">
                        </div>
                    </td>
                `;
            }
        }*/
        function createTurnWiseContributorRow(rowIndex, contributorIndex, symbol) {
            tableData[rowIndex][0] = `<td class="regular-height"><img src="http://localhost:8000/img/${turnWiseContributors[contributorIndex].texture}.png" alt="${turnWiseContributors[contributorIndex].type}" class="score-icon"></td>`;
            tableData[rowIndex][1] = '<td class="regular-height"><input type="number" class="score-input"></td>';
            tableData[rowIndex][2] = '<td class="regular-height score-table-turnwise-border"></td>';
            tableData[rowIndex][3] = '<td class="regular-height"><input type="number" class="score-input"></td>';
            tableData[rowIndex][4] = '<td class="regular-height score-table-turnwise-border"></td>';
            tableData[rowIndex][5] = '<td class="regular-height"><input type="number" class="score-input"></td>';
            tableData[rowIndex][6] = '<td class="regular-height score-table-turnwise-border"></td>';
            tableData[rowIndex][7] = '<td class="regular-height"><input type="number" class="score-input"></td>';
            tableData[rowIndex][8] = '<td class="regular-height score-table-turnwise-border"></td>';

            tableData[rowIndex + 1][1] = `<td class="score-table-operation-symbol">${symbol}</td>`;
            tableData[rowIndex + 1][2] = `<td class="score-table-turnwise-border"></td>`;
            tableData[rowIndex + 1][3] = `<td class="score-table-operation-symbol">${symbol}</td>`;
            tableData[rowIndex + 1][4] = `<td class="score-table-turnwise-border"></td>`;
            tableData[rowIndex + 1][5] = `<td class="score-table-operation-symbol">${symbol}</td`;
            tableData[rowIndex + 1][6] = `<td class="score-table-turnwise-border"></td>`;
            tableData[rowIndex + 1][7] = `<td class="score-table-operation-symbol">${symbol}</td>`;
        }

        createTurnWiseContributorRow(0, 0, "×");
        createTurnWiseContributorRow(2, 1, "+");
        createTurnWiseContributorRow(4, 2, "=");

        // sum cells
        tableData[6][1] = '<td class="regular-height"><input type="number" class="score-input"></td>';
        tableData[6][2] = '<td class="regular-height width-none score-table-operation-symbol">+</td>';
        tableData[6][3] = '<td class="regular-height"><input type="number" class="score-input"></td>';
        tableData[6][4] = '<td class="regular-height width-none score-table-operation-symbol">+</td>';
        tableData[6][5] = '<td class="regular-height"><input type="number" class="score-input"></td>';
        tableData[6][6] = '<td class="regular-height width-none score-table-operation-symbol">+</td>';
        tableData[6][7] = '<td class="regular-height"><input type="number" class="score-input"></td>';
        tableData[6][8] = '<td class="regular-height width-none score-table-operation-symbol">=</td>';
        tableData[6][9] = '<td class="regular-height"><input type="number" class="score-input"></td>';

        function createEndGameContributorRow(rowIndex, contributorIndex) {
            tableData[rowIndex][9] = `<td class="regular-height"><img src="http://localhost:8000/img/${endGameContributors[contributorIndex].texture}.png" alt="${endGameContributors[contributorIndex].type}" class="score-icon"></td>`;
            tableData[rowIndex][10] = '<td class="regular-height">×</td>';
            tableData[rowIndex][11] = '<td class="regular-height"><input type="number" class="score-input"></td>';
            tableData[rowIndex][12] = '<td class="regular-height">=</td>';
            tableData[rowIndex][13] = '<td class="regular-height"><input type="number" class="score-input"></td>';
        }

        createEndGameContributorRow(0, 0);
        createEndGameContributorRow(2, 1);
        createEndGameContributorRow(4, 2);


        // Construct the table using the array
        for (let i = 0; i < tableData.length; i++) {
            const row = scoreTable.insertRow();
            for (let j = 0; j < tableData[i].length; j++) {
                if (tableData[i][j]) {
                    row.innerHTML += tableData[i][j];
                } else {
                    row.insertCell();
                }
            }
        }
    }
}

// Initialize the game when the DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('gameCanvas');
    new GameBoard(canvas);
});

