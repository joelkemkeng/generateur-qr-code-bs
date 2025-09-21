cree une API javaspring booot

surtout Dockeriser cela , 

avec 2 end point 

1 qui consiste a checker la santer de API 
1 qui consiste a  a prendre en paramettre une reference un String chaine de caractere , qui vas par la suite generer un PDF qui contien en titre la reference ; et le code QR generer , sauvegarder le document pdf cela dans un dossier  "document-qr-code-generer"
le endpoint doit retourner en reponse le path url du document generer , tel que si je l'ouvre dans mon navigateur le document s'ouvre 



rassure toi de ne pas trop compliquer les chose ; 
fais le aussi simplement que possible ,
la seul chose qui devrait etre tres pointilleur ici , c'st le code QR qui doit etre d'un dessign vrqiement unique exqctement comme celui generer par "/home/joel/projet-boaz-housing/boaz-housing-mvp/backend/app/services/attestation_generator.py"  je dit bien exactement comme le code QR generer par ce fichier python ; le meme design , 

et il faut bien faire la gestion des erreur , et des reponses en cas d'erreur ...etc 


bon fais le tel que a la fin je lance un docker compose up ; et boommm  je peux tester , tu me donne les recommandations a suivre ou guide pour tester 

pendant le develeppement n'oublie surtour pas de faire des test unitaire , integration au fure et a mesure que tu avance , tu teste toi meme au fur et a mesure pour eviter les erreur a venir  ; afin de ne pas tout faire en meme temps et avoir des erreur a debuguer apres ;   je veux eviter les debugage 

fait attention a fabriquer n'importe quoi qui puisse ne servir a rien ; et ca doit bien fonctionner surtout 

et comme j'ai dit, rassurer vous du respect du style du code QR ; c'est l'element le plus important 

