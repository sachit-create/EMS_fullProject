const fs = require('fs');
const path = require('path');
const Database = require('better-sqlite3');

const dbDir = path.join(process.cwd(), 'data');
const dbPath = path.join(dbDir, 'ems.db');

fs.mkdirSync(dbDir, { recursive: true });

const db = new Database(dbPath);
db.pragma('journal_mode = WAL');

function initializeDatabase() {
  db.exec(`
    CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      username TEXT UNIQUE NOT NULL,
      password TEXT NOT NULL,
      email TEXT UNIQUE NOT NULL,
      full_name TEXT NOT NULL,
      created_at TEXT DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS employees (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      employee_code TEXT UNIQUE NOT NULL,
      name TEXT NOT NULL,
      father_name TEXT NOT NULL,
      email TEXT UNIQUE,
      gender TEXT NOT NULL,
      salary REAL NOT NULL DEFAULT 0,
      department TEXT,
      designation TEXT,
      photo_data_url TEXT,
      created_at TEXT DEFAULT CURRENT_TIMESTAMP
    );
  `);
}

initializeDatabase();

module.exports = { db, initializeDatabase };
