# 🤖 RepoPilot AI

> **AI-Powered GitHub Repository Analysis Platform**
>
> Analyze any public GitHub repository with AI-powered architecture review, security insights, code quality analysis, repository-aware chat, and portfolio guidance.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-green)
![React](https://img.shields.io/badge/React-19-61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1)
![Spring AI](https://img.shields.io/badge/Spring%20AI-6DB33F)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## 🚀 Live Demo

**Frontend**

https://repo-pilot-ai-git-main-jagruti-deores-projects.vercel.app/

**Backend API**

https://repopilot-ai.onrender.com

---

# 📌 Overview

RepoPilot AI is a full-stack AI-powered developer platform that helps developers understand GitHub repositories through automated code analysis and AI-assisted explanations.

Instead of manually exploring an unfamiliar repository, users can import a GitHub repository and instantly receive:

- Architecture Review
- Security Analysis
- Code Quality Insights
- Repository Readiness Score
- AI Repository Chat
- Portfolio Coach
- Resume Bullet Suggestions
- README Guidance
- Interview Preparation
- Analysis History

The project combines traditional software engineering techniques with modern AI capabilities to create an intelligent developer assistant.

---

# ✨ Features

## 🔐 Authentication

- JWT Authentication
- Secure Login & Registration
- Protected Routes
- Spring Security

---

## 📂 Repository Import

- Import public GitHub repositories
- GitHub API integration
- Store repository metadata
- Parse project files
- Persist repository information

---

## 📊 Repository Analysis

Automatically analyzes:

- Architecture
- Security
- Maintainability
- Documentation
- Testing
- Code Quality

Generates:

- Overall Project Score
- Readiness Score
- Repository Findings

---

## 🤖 AI Features

Implemented AI capabilities include:

- Spring AI
- Ollama (Local LLM)
- Prompt Engineering
- Repository-aware AI Chat
- Structured AI Responses
- AI-powered Architecture Explanation
- Security Recommendations
- Resume Bullet Generation
- README Suggestions
- Interview Preparation

---

## 💬 Repository Chat

Ask questions such as:

- Explain the architecture.
- How is JWT implemented?
- Which file handles authentication?
- Explain this service.
- What does this controller do?
- Suggest improvements.
- Explain project flow.

---

## 🎯 Portfolio Coach

Generate:

- Resume Summary
- Resume Bullets
- LinkedIn Description
- README Suggestions
- Project Title Ideas
- Presentation Script
- Viva Preparation
- Interview Questions

---

## 📈 Analysis Dashboard

Includes:

- Overview
- Architecture
- Security
- Code Quality
- Findings
- File Explorer
- History
- Export Report

---

# 🧠 AI Architecture

```
                GitHub Repository
                        │
                        ▼
             GitHub API Integration
                        │
                        ▼
              Spring Boot Backend
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
 Static Analysis   Spring AI      PostgreSQL
        │               │               │
        ▼               ▼               ▼
 Repository Scores  Ollama LLM   Store Results
        │
        ▼
 React Dashboard
```

---

# 🏗️ Tech Stack

## Frontend

- React
- TypeScript
- Vite
- Tailwind CSS
- Axios
- Lucide React

---

## Backend

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- JWT Authentication
- REST APIs
- Maven

---

## AI

- Spring AI
- Ollama
- Prompt Engineering

---

## Database

- PostgreSQL
- pgvector-ready setup

---

## Infrastructure

- Docker
- Render
- Vercel
- GitHub API

---

# 📂 Project Structure

```
RepoPilot-AI/

├── backend/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── security/
│   ├── ai/
│   └── config/
│
├── frontend/
│   ├── pages/
│   ├── components/
│   ├── hooks/
│   ├── context/
│   ├── services/
│   └── layouts/
│
├── docker-compose.yml
└── README.md
```

---

# ⚙️ Running Locally

## Clone

```bash
git clone https://github.com/Jagruti2004deore/RepoPilot-AI.git

cd RepoPilot-AI
```

---

## Start PostgreSQL

```bash
docker compose up -d
```

---

## Backend

```bash
cd backend

./mvnw spring-boot:run
```

Windows

```powershell
.\mvnw.cmd spring-boot:run
```

---

## Frontend

```bash
cd frontend

npm install

npm run dev
```

Frontend:

```
http://localhost:5173
```

Backend:

```
http://localhost:8080/api
```

---

# 🔄 Application Flow

```
User

↓

React Frontend

↓

REST API

↓

Spring Boot

↓

Controller

↓

Service

↓

Repository

↓

PostgreSQL

↓

Spring AI

↓

Ollama

↓

AI Response

↓

Dashboard
```

---

# 📌 Main APIs

```
POST /api/auth/register

POST /api/auth/login

POST /api/repositories/import

GET /api/repositories

GET /api/repositories/{id}/analysis

GET /api/repositories/{id}/files

POST /api/repositories/{id}/reanalyze

POST /api/repositories/{id}/chat/ask
```

---

# 🌍 Language Support

RepoPilot AI analyzes repositories by understanding source code and project structure using AI.

Examples include:

- Java
- JavaScript
- TypeScript
- Python
- C
- C++
- C#
- Go
- Kotlin
- PHP
- Rust

Java and Spring Boot projects receive richer framework-aware insights where applicable.

---

# 🔮 Future Enhancements

- GitHub OAuth
- Full RAG with Vector Search
- Streaming AI Responses
- Multi-Repository Comparison
- PDF Report Export
- Team Collaboration
- Cloud AI Provider Support
- Advanced MCP Integration

---

# 👩‍💻 Author

**Jagruti Deore**

- LinkedIn: https://linkedin.com/in/jagruti-deore
- Portfolio: https://portfolio-jagruti.onrender.com
- GitHub: https://github.com/Jagruti2004deore

---

⭐ If you found this project useful, consider giving it a star!
