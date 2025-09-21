# Générateur de QR Codes Boaz-Housing

**Auteur : Joel Kemkeng**

Une API REST professionnelle développée avec Spring Boot pour la génération de QR codes avec le style unique Boaz-Housing. Cette solution offre deux formats de génération : PDF pour documents officiels et images PNG avec accès complet (URL, nom de fichier, base64).

## 🎯 Objectif du Projet

Développer une API moderne et performante qui génère des QR codes avec un design personnalisé intégrant :
- Points bleus circulaires pour les données
- Coins orange avec dégradé pour les finder patterns
- Logo central Boaz-Housing avec fond circulaire
- Support PDF et image selon les besoins

## ✨ Fonctionnalités Principales

### 📄 Génération PDF
- QR code intégré dans un PDF professionnel
- Métadonnées complètes du document
- Téléchargement direct via navigateur
- Gestion automatique des noms de fichiers

### 🖼️ Génération Images (Nouveau)
- Service isolé et réutilisable
- Retour complet : URL d'accès, nom de fichier, image base64
- Intégration facile dans d'autres applications
- Support haute qualité PNG

### 🔧 Fonctionnalités Techniques
- Configuration Docker complète
- Documentation Swagger intégrée
- Validation robuste des entrées
- Logs détaillés pour monitoring
- Architecture modulaire et maintenable

## 🏗️ Architecture Technique

### Stack Technologique
- **Framework** : Spring Boot 3.2.x
- **Java** : OpenJDK 17
- **Build** : Maven 3.9.x
- **Conteneurisation** : Docker + Docker Compose
- **Documentation** : OpenAPI 3 / Swagger

### Structure du Projet
```
├── src/main/java/com/boazhousing/qrcodegen/
│   ├── controller/          # Contrôleurs REST
│   ├── service/             # Logique métier
│   ├── model/               # Modèles de données
│   └── config/              # Configuration Spring
├── src/main/resources/
│   ├── static/assets/       # Logo et ressources
│   └── application.yml      # Configuration application
└── docker/                  # Configuration Docker
```

### Services Principaux

#### `BoazQrCodeService`
Service historique pour génération PDF avec QR code intégré.

#### `ImageQrCodeService` (Nouveau)
Service isolé et réutilisable pour génération d'images :
- Génération QR code stylisé
- Sauvegarde automatique sur disque
- Conversion base64 intégrée
- Gestion des URLs d'accès

#### `ImageController`
Contrôleur dédié pour service des images générées avec validation sécurisée.

## 📋 Endpoints API

### 1. Génération PDF
```http
GET /generate-qr?reference=ATT-DOCUMENT-001
```
**Réponse** : Métadonnées avec lien de téléchargement PDF

### 2. Génération Image (Nouveau)
```http
GET /generate-qr-image?reference=ATT-DOCUMENT-001
```
**Réponse** :
```json
{
  "success": true,
  "data": {
    "accessUrl": "http://localhost:8080/images/QR_IMG_ATT-DOCUMENT-001_20240321_143022.png",
    "imageName": "QR_IMG_ATT-DOCUMENT-001_20240321_143022.png",
    "base64Image": "iVBORw0KGgoAAAANSUhEUgAAA...",
    "reference": "ATT-DOCUMENT-001",
    "base64Size": 45678
  }
}
```

### 3. Accès Direct Images
```http
GET /images/{filename}
```
**Réponse** : Image PNG avec en-têtes optimisés

### 4. Documentation
```http
GET /swagger-ui.html
```
Interface Swagger complète pour test et documentation

## 🚀 Installation et Lancement

### Prérequis
- Docker et Docker Compose
- Port 8080 disponible

### Démarrage Rapide
```bash
# Clone du projet
git clone <repository>
cd api-generateur-qr-code-bs

# Lancement avec Docker
docker-compose up --build

# Vérification du statut
curl http://localhost:8080/health
```

### Utilisation en Développement
```bash
# Build uniquement
docker-compose build

# Logs en temps réel
docker-compose logs -f

# Arrêt propre
docker-compose down
```

## 🔧 Configuration

### Variables d'Environnement
```yaml
# URL de base pour les QR codes
QR_BASE_URL=https://housing.boaz-study.tech

# URL d'accès aux images générées
IMAGE_BASE_URL=http://localhost:8080/images
```

### Couleurs Boaz-Housing
```yaml
app:
  colors:
    primary-blue: "#0140ff"    # Points de données
    orange-dark: "#f88206"     # Centre des corners
    orange-medium: "#fa9000"   # Milieu des corners
    orange-light: "#ffa94d"    # Bordure des corners
```

## 📝 Tests et Validation

### Tests Manuels Rapides
```bash
# Test génération PDF
curl "http://localhost:8080/generate-qr?reference=TEST-001"

# Test génération image
curl "http://localhost:8080/generate-qr-image?reference=TEST-001"

# Test accès direct image
curl -I "http://localhost:8080/images/QR_IMG_TEST-001_[timestamp].png"

# Test documentation
curl "http://localhost:8080/swagger-ui.html"
```

### Validation QR Codes
- Scanner mobile pour vérifier la lisibilité
- Validation des URLs générées
- Test de résistance aux erreurs

## 📊 Monitoring et Logs

### Endpoints de Santé
- `/health` : Santé générale de l'application
- `/images/health` : Statut du service d'images

### Logs Applicatifs
```bash
# Logs du conteneur
docker-compose logs qr-code-generator

# Logs temps réel
docker-compose logs -f
```

## 🔒 Sécurité

### Validation des Entrées
- Regex stricte pour les références
- Limitation de longueur (50 caractères)
- Protection contre path traversal
- Validation MIME type pour images

### Gestion d'Erreurs
- Messages d'erreur non sensibles
- Logs détaillés côté serveur
- Codes HTTP appropriés

## 🚀 Déploiement Production

### Optimisations Recommandées
- Reverse proxy (nginx) pour assets statiques
- Stockage externe pour images (S3, etc.)
- Load balancing pour haute disponibilité
- Monitoring avec Prometheus/Grafana

### Variables Production
```bash
export QR_BASE_URL=https://production.boaz-housing.com
export IMAGE_BASE_URL=https://cdn.boaz-housing.com/qr-images
```

## 📈 Performance

### Métriques Clés
- Génération PDF : ~2-3 secondes
- Génération Image : ~1-2 secondes
- Taille moyenne QR : ~15KB (PNG)
- Mémoire JVM : ~512MB

### Optimisations
- Cache des logos en mémoire
- Compression PNG optimisée
- Pool de connexions configuré
- GC tuning pour faible latence

## 🤝 Contribution

### Standards de Code
- Checkstyle configuré
- Tests unitaires obligatoires
- Documentation Javadoc complète
- Messages de commit conventionnels

### Architecture
- Services isolés et réutilisables
- Séparation des responsabilités
- Configuration externalisée
- Logs structurés

## 📞 Support

Pour toute question ou problème :
- **Développeur** : Joel Kemkeng
- **Documentation** : `/swagger-ui.html`
- **Logs** : `docker-compose logs`

---

*Développé avec attention aux détails pour Boaz-Housing - 2024*