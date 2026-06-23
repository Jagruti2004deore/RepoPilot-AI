# RepoLensAI

RepoLensAI is an AI-powered code review and architecture analyzer for GitHub repositories. The product is designed to behave like an AI staff engineer: it imports real source code, evaluates architecture and code quality, highlights security risks, scores project readiness, and prepares interview and resume outputs.

## Day 1 Status

The first-day foundation is complete:

- Spring Boot backend scaffold with Java 21 target
- JWT authentication foundation
- PostgreSQL configuration
- User, repository, analysis, and chat history data models
- Repository import API skeleton
- React TypeScript frontend scaffold
- Dark professional dashboard UI
- Login/register UI
- GitHub repository import form
- Docker Compose database setup with PostgreSQL + PGVector

## Tech Stack

Backend:

- Java 21
- Spring Boot 3
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL
- Maven wrapper

Frontend:

- React
- TypeScript
- Vite
- Tailwind CSS
- React Query
- Axios
- Lucide React

AI roadmap:

- Ollama
- Gemma/Qwen local models
- Spring AI
- PGVector RAG pipeline

## Project Structure

```text
RepoLens AI/
  backend/
  frontend/
  docker-compose.yml
  README.md
```

## Run Locally

Start PostgreSQL:

```bash
docker compose up -d
```

Run backend:

```bash
cd backend
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Run frontend:

```bash
cd frontend
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

Backend health URL:

```text
http://localhost:8080/api/actuator/health
```

## Seven-Day Roadmap

Day 1: project foundation, auth, dashboard shell, repository import skeleton.

Day 2: GitHub REST API file fetching, repository tree parsing, source file extraction, and analysis history.

Day 3: static code analysis engine for field injection, missing validation, long methods, hardcoded secrets, weak exception handling, and package structure.

Day 4: Spring AI structured architecture, code quality, security, and scoring reports.

Day 5: RAG with chunking, embeddings, PGVector, similarity search, and repository-aware chat memory.

Day 6: full frontend product experience with analysis pages, score cards, charts, interview questions, resume generator, and AI chat.

Day 7: tests, polish, screenshots, deployment notes, and interview-ready documentation.

