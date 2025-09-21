# 🧪 Guide de Test - QR Code Generator API

## 🚀 Démarrage Rapide

### 1. Lancer l'application
```bash
cd /home/joel/projet-boaz-housing/api-generateur-qr-code-bs
docker-compose up --build
```

### 2. Attendre le démarrage complet
Rechercher dans les logs :
```
Started QrCodeGeneratorApplication in X.XXX seconds
```

## ✅ Tests Essentiels

### Test 1 : Health Check
```bash
curl http://localhost:8080/health
```
**Résultat attendu :** Status 200 avec `"success": true`

### Test 2 : Génération QR simple
```bash
curl "http://localhost:8080/generate-qr?reference=ATT-TEST123"
```
**Résultat attendu :** JSON avec `pdfUrl` et `success: true`

### Test 3 : Ouverture PDF dans navigateur
Copier l'URL `pdfUrl` du test précédent et l'ouvrir dans le navigateur.
**Résultat attendu :** PDF s'ouvre avec titre "ATT-TEST123" et QR code stylé

### Test 4 : Validation erreur - référence vide
```bash
curl "http://localhost:8080/generate-qr?reference="
```
**Résultat attendu :** Status 400 avec `"success": false`

### Test 5 : Validation erreur - pas de paramètre
```bash
curl "http://localhost:8080/generate-qr"
```
**Résultat attendu :** Status 400

### Test 6 : Liste des fichiers générés
```bash
curl http://localhost:8080/list-generated
```
**Résultat attendu :** Array avec les noms de fichiers

## 🎯 Validation Style QR

### Vérifier visuellement :
1. Générer un QR : `curl "http://localhost:8080/generate-qr?reference=ATT-STYLE-TEST"`
2. Ouvrir le PDF dans le navigateur
3. **Vérifier :**
   - ✅ Points de données = **CERCLES BLEUS** (pas carrés)
   - ✅ Coins de détection = **CARRÉS ORANGE** avec dégradé
   - ✅ Logo central = **"BH"** blanc sur fond circulaire
   - ✅ QR code scannable avec smartphone

### Test de scan :
1. Scanner le QR avec un smartphone
2. **URL attendue :** `https://housing.boaz-study.tech/verif_doc?ref=ATT-STYLE-TEST`

## 🔧 Tests de Performance

### Test stress - Génération multiple
```bash
for i in {1..10}; do
  curl "http://localhost:8080/generate-qr?reference=ATT-TEST-$i" &
done
wait
```

### Test taille fichiers
```bash
# Vérifier que les PDFs sont de taille raisonnable (< 100KB)
ls -la document-qr-code-generer/
```

## 🐛 Debugging

### Si l'API ne démarre pas :
```bash
# Vérifier les logs
docker-compose logs qr-code-generator

# Vérifier les ports
netstat -tulpn | grep 8080

# Redémarrer proprement
docker-compose down && docker-compose up --build
```

### Si les QR codes ne se génèrent pas :
```bash
# Vérifier les permissions du répertoire
ls -la document-qr-code-generer/

# Vérifier les logs d'erreur
docker-compose logs qr-code-generator | grep ERROR
```

### Si les PDFs ne s'ouvrent pas :
```bash
# Tester téléchargement direct
curl -o test.pdf "http://localhost:8080/download/[filename-from-response]"

# Vérifier que le fichier est bien créé
file test.pdf  # Doit indiquer "PDF document"
```

## 🎯 Checklist de Validation Complète

- [ ] ✅ Health Check répond correctement
- [ ] ✅ Génération QR avec référence valide
- [ ] ✅ PDF créé dans `document-qr-code-generer/`
- [ ] ✅ URL de téléchargement fonctionne
- [ ] ✅ PDF s'ouvre dans navigateur
- [ ] ✅ QR code visible et stylé correctement
- [ ] ✅ QR code scannable (test smartphone)
- [ ] ✅ URL de vérification correcte
- [ ] ✅ Gestion erreurs (référence vide)
- [ ] ✅ Gestion erreurs (paramètre manquant)
- [ ] ✅ Liste des fichiers générés
- [ ] ✅ Style visuel exact (cercles bleus, coins orange, logo BH)

## 🎉 Critères de Succès

**L'API est validée si :**
1. Tous les tests passent ✅
2. QR codes générés sont **visuellement identiques** au style Python
3. PDFs s'ouvrent correctement dans le navigateur
4. QR codes sont scannables et pointent vers la bonne URL
5. Gestion d'erreurs fonctionne proprement

## 📞 Support

En cas de problème :
1. Vérifier les logs : `docker-compose logs qr-code-generator`
2. Vérifier la checklist ci-dessus
3. Redémarrer : `docker-compose down && docker-compose up --build`

**L'API est prête pour la production dès que tous les tests passent !** 🚀