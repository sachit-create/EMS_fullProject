# Employee Management System

A first useful version of the project designed for Vercel-friendly deployment.

Features:
- React frontend with admin login
- employee CRUD dashboard
- seeded employee data
- AI summary generation using Ollama-compatible endpoint
- Vercel-ready app structure with in-memory demo store for first deployment

Default admin login:
- Username: admin
- Password: admin123
- Email: admin@home.dev

Local run:
```bash
npm install
npm run dev
```

Open:
http://localhost:3000

Vercel deployment:
- Deploy this repository to Vercel
- Set environment vars if needed for future hosted database or Ollama endpoint
- The first version is built to run without a local database or native modules, making it easier to deploy on Vercel
