# RepoPilot AI

RepoPilot AI is a full-stack GitHub repository review and project-readiness mentor. It imports real repository files, runs rule-based static analysis, scores architecture/security/maintainability/docs/tests, and turns the result into portfolio, resume, viva, interview, and README guidance.

## Completed Features

- JWT register/login flow
- GitHub repository import from a public URL
- PostgreSQL persistence for users, repositories, files, and analysis runs
- Static analysis for architecture layers, validation, security risks, secrets, logging, TODOs, large files, and pagination hints
- Scorecards for architecture, security, maintainability, documentation, testing, and overall quality
- Readiness scorecard for resume, interview, GitHub, deployment, and demo readiness
- File inventory with role and signal detection
- Re-analysis from saved repository files
- Analysis history comparison panel
- Coach tabs for interview answers, viva prep, presentation script, architecture explanation, resume bullets, GitHub profile tips, README suggestions, and project title ideas
- Copy buttons for coach/readiness/resume sections
- Markdown report export from the frontend
- RepoPilot AI branding across the app UI and browser metadata
- Light coder-focused product homepage with services and usage diagram
- Repo-aware Q&A assistant for asking questions about imported code

## Tech Stack

Backend:

- Java 21 target
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
- Axios
- Lucide React

Infrastructure:

- Docker Compose
- PostgreSQL with pgvector image

## Project Structure

```text
RepoLens AI/
  backend/
  frontend/
  docker-compose.yml
  README.md
```

The local folder and GitHub repository can still keep the old path name. The product name shown to users is RepoPilot AI.

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

Backend base URL:

```text
http://localhost:8080/api
```

## Main API Flow

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/repositories/import`
- `GET /api/repositories`
- `GET /api/repositories/{repositoryId}/analysis`
- `GET /api/repositories/{repositoryId}/analyses`
- `GET /api/repositories/{repositoryId}/files`
- `POST /api/repositories/{repositoryId}/reanalyze`
- `GET /api/repositories/{repositoryId}/chat`
- `POST /api/repositories/{repositoryId}/chat/ask`

## Verification

Backend build:

```powershell
cd backend
.\mvnw.cmd -DskipTests package
```

Frontend build:

```powershell
cd frontend
npm run build
```

## Unique Angle

RepoPilot AI is not only a code review dashboard. It acts like a project mentor for students and portfolio builders by combining technical review with interview preparation, resume bullets, README coaching, demo readiness, analysis history comparison, and repo-aware code Q&A in one workflow.