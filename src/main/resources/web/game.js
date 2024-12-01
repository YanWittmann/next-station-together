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
        const container = this.canvas.parentElement;
        const size = container.clientWidth;
        this.canvas.width = size;
        this.canvas.height = size;

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

        this.drawRiver();
        this.drawDistricts();
        this.drawPrebakedConnections();
        this.drawUserConnections();
        this.drawStations();
        this.drawIntersections();

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

    getStationAt(x, y) {
        const gridX = Math.floor(x / (this.STATION_SIZE * this.scale));
        const gridY = Math.floor(y / (this.STATION_SIZE * this.scale));

        return this.boardData.stations.find(
            station => station.x === gridX && station.y === gridY
        );
    }

    handleMouseDown(e) {
        const rect = this.canvas.getBoundingClientRect();
        const x = (e.clientX - rect.left) / this.scale;
        const y = (e.clientY - rect.top) / this.scale;

        const station = this.getStationAt(x, y);
        if (station) {
            this.dragStart = {
                x: station.x,
                y: station.y,
                station: station
            };
            this.currentConnection = {
                x: station.x,
                y: station.y
            };
        }
    }

    handleMouseMove(e) {
        if (!this.dragStart) return;

        const rect = this.canvas.getBoundingClientRect();
        const x = (e.clientX - rect.left) / this.scale;
        const y = (e.clientY - rect.top) / this.scale;

        const gridX = Math.floor(x / this.STATION_SIZE);
        const gridY = Math.floor(y / this.STATION_SIZE);

        this.currentConnection = { x: gridX, y: gridY };
        this.draw();
    }

    handleMouseUp(e) {
        if (!this.dragStart || !this.currentConnection) {
            this.dragStart = null;
            this.currentConnection = null;
            return;
        }

        const endStation = this.getStationAt(
            this.currentConnection.x * this.STATION_SIZE,
            this.currentConnection.y * this.STATION_SIZE
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
}

// Initialize the game when the DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('gameCanvas');
    new GameBoard(canvas);
});
