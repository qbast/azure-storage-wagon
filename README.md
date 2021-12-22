# Azure Storage Wagon

Azure Storage Wagon allows using Azure Blob Storage as a maven repository. It supports downloading and uploading 
artifacts with multiple types of authentication.

## Usage

Start by adding a dependency on the azure wagon.
It can be done in pom.xml file like this:

```
<build>
    <extensions>
        <extension>
            <groupId>io.github.qbast</groupId>
            <artifactId>azure-storage-wagon</artifactId>
            <version>1.0.1</version>
        </extension>
    </extensions>
</build>
```

Another way is to create `.mvn/extensions.xml` in the project directory with following contents:

```
<extensions xmlns="http://maven.apache.org/EXTENSIONS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/EXTENSIONS/1.0.0 http://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
       <extension>
           <groupId>io.github.qbast</groupId>
           <artifactId>azure-storage-wagon</artifactId>
           <version>1.0.1</version>
        </extension>
    </extensions>

```
This allows to use an artifact stored in Azure Blob Storage as a parent.

With this dependency added, it is possible to configure Azure Blob repositories in <repositories> or <dependencyManagement> section of the pom.xml

```xml
<distributionManagement>
    <snapshotRepository>
        <id>my-repo-bucket-snapshot</id>
        <url>bs://storage_account/container/snapshot</url>
    </snapshotRepository>
    <repository>
        <id>my-repo-bucket-release</id>
        <url>bs://storage_account/container/release</url>
    </repository>
</distributionManagement>
```

Blob Storage URLs have the following structure: `bs://<storage account name>/<container>/<directory>` 

## Authentication

Azure Storage Wagon supports multiple types of authentication making it suitable for many scenarios: simple access via account key,
 CI machines using managed identity, developers' machines using azure CLI or external CI servers using service principals.

### Account key authentication

A simple way to configure repository authentication is to add storage account primary or secondary key to ~/.m2/settings.xml

```
<servers>
  <server>
    <id>my-repo-bucket-snapshot</id>
    <username>mavenrepository</username>
    <password>eXampLEkeyEMI/K7EXAMP/bPxRfiCYEXAMPLEKEY</password>
  </server>
  <server>
    <id>my-repo-bucket-release</id>
    <username>mavenrepository</username>
    <password>eXampLEkeyEMI/K7EXAMP/bPxRfiCYEXAMPLEKEY</password>
  </server>
</servers>
```

`<id>` should match repository id from `<repositories>` tag in pom.xml. Username is ignored and password should be either primary or secondary key of the storage account.

### Authentication based on environment variables

If authentication is not set in settings.xml, the wagon will attempt several ways of authentication until one succeeds.
The first one is base on environment variables. This mode supports three different types of credentials:

#### Service principal with secret
 - AZURE_CLIENT_ID - id of an Azure Active Directory application
 - AZURE_TENANT_ID - id of the application's Azure Active Directory tenant
 - AZURE_CLIENT_SECRET - one of the application's client secrets

#### Service principal with certificate
- AZURE_CLIENT_ID - id of an Azure Active Directory application
- AZURE_TENANT_ID - id of the application's Azure Active Directory tenant
- AZURE_CLIENT_CERTIFICATE_PATH - path to a PEM-encoded certificate file including private key (without password protection)

#### Username and password
- AZURE_CLIENT_ID - id of an Azure Active Directory application
- AZURE_USERNAME - a username (usually an email address)
- AZURE_PASSWORD- that user's password

### Authentication using managed identity

This is useful only on VMs running in Azure Cloud with Managed Identity. No configuration is needed, the wagon will pick up
the identity automatically.

### Authentication using Azure CLI

This is the authentication method that gets attempted last. It will pick up user's token if that user has authenticated using
`az login` command first. It is intended for using on developers' machines.

