В данном пет-проекте реализовано [шифрование карт
с помощью алгоритма AES](./src/main/java/kg/musabaev/em_bank_rest/util/SomePaymentSystemProvider.java), но в реальных проектах
лучше использовать готовые сервисы вроде AWS KMS, Azure Key Vault или HashiCorp Vault
для безопасного хранения ключей и шифрования данных

https://habr.com/ru/articles/818489/

https://stackoverflow.com/questions/27767264/how-to-dockerize-a-maven-project-how-many-ways-to-accomplish-it