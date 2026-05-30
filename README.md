# ShopFlow — Système de Gestion d'une Boutique en Ligne

Plateforme e-commerce B2C avec backend Spring Boot 3 et frontend Next.js.

## Stack Technique

| Couche | Technologie |
|--------|-------------|
| Backend | Java 21, Spring Boot 3.2, Spring Security 6 + JWT |
| Base de données | H2 (dev) / PostgreSQL (prod) |
| ORM | Spring Data JPA, Hibernate |
| Mapping | MapStruct |
| Documentation | Springdoc OpenAPI 3 (Swagger UI) |
| Frontend | Next.js 14, TypeScript, Tailwind CSS |
| État | Zustand, TanStack Query |

## Prérequis

- Java 21+
- Maven 3.9+
- Node.js 18+
- npm ou yarn

## Lancement Backend

```bash
cd shopflow/backend

# Mode développement (H2 en mémoire)
mvn spring-boot:run

# Mode production (PostgreSQL)
mvn spring-boot:run -Dspring.profiles.active=prod
```

Le backend démarre sur **http://localhost:8082**

- Swagger UI : http://localhost:8082/swagger-ui
- H2 Console (dev) : http://localhost:8082/h2-console

## Lancement Frontend

```bash
cd shopflow/frontend
npm install
npm run dev
```

Le frontend démarre sur **http://localhost:3000**

## Structure du Projet

```
shopflow/
├── backend/
│   └── src/main/java/com/shopflow/
│       ├── entity/          # Entités JPA
│       ├── repository/      # Spring Data JPA
│       ├── service/         # Logique métier
│       ├── controller/      # REST Controllers
│       ├── dto/             # Request/Response DTOs
│       ├── security/        # JWT + Spring Security
│       ├── exception/       # Gestion des erreurs
│       └── config/          # OpenAPI config
└── frontend/
    └── src/
        ├── app/             # Pages Next.js (App Router)
        ├── components/      # Composants réutilisables
        ├── hooks/           # Custom hooks
        ├── store/           # Zustand stores
        ├── lib/             # Axios + intercepteurs
        └── types/           # TypeScript types
```

## Endpoints API Principaux

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | /api/auth/register | Inscription |
| POST | /api/auth/login | Connexion |
| GET | /api/products | Catalogue paginé |
| GET | /api/products/search?q= | Recherche |
| POST | /api/cart/items | Ajouter au panier |
| POST | /api/orders | Passer une commande |
| GET | /api/dashboard/admin | Stats admin |
| GET | /api/dashboard/seller | Stats vendeur |

## Rôles Utilisateurs

- **ADMIN** : gestion globale, utilisateurs, catégories, modération
- **SELLER** : gestion boutique, produits, commandes reçues
- **CUSTOMER** : catalogue, panier, commandes, avis

## Tests

```bash
cd shopflow/backend
mvn test
```

## Build Production

```bash
# Backend
cd shopflow/backend
mvn clean package -DskipTests

# Frontend
cd shopflow/frontend
npm run build
```
