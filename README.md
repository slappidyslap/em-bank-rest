В данном пет-проекте реализовано [шифрование карт
с помощью алгоритма AES](./src/main/java/kg/musabaev/em_bank_rest/util/CardNumberConverter.java), но в реальных проектах
лучше использовать готовые сервисы вроде AWS KMS, Azure Key Vault или HashiCorp Vault
для безопасного хранения ключей и шифрования данных