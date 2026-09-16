# BDSI Commerce Project - Design Patterns Complete Guide

## Java / Spring Boot / Microservices Design Patterns Analysis

**Project:** BDSI Commerce Platform (Oracle ATG Commerce)  
**Date:** September 15, 2026  
**Author:** Shwetha Kumar

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Architecture Overview](#2-architecture-overview)
3. [Creational Patterns](#3-creational-patterns)
4. [Structural Patterns](#4-structural-patterns)
5. [Behavioral Patterns](#5-behavioral-patterns)
6. [Enterprise Patterns](#6-enterprise-patterns)
7. [Microservices Patterns](#7-microservices-patterns)
8. [Code Flow Diagrams](#8-code-flow-diagrams)
9. [Pattern Summary Table](#9-pattern-summary-table)
10. [Interview Quick Reference](#10-interview-quick-reference)

---

## 1. Executive Summary

This document provides a comprehensive analysis of all Java/Spring Boot/Microservices design patterns implemented in the BDSI Commerce project. The project is built on **Oracle ATG Commerce Platform** with Java, incorporating many classic Gang of Four (GoF) patterns and modern enterprise integration patterns.

### Key Findings:
- **20+ Design Patterns** identified across the codebase
- **Layered Architecture** with clear separation of concerns
- **Enterprise Integration Patterns** for messaging and external systems
- **Cloud-Native Patterns** for Azure integration

---

## 2. Architecture Overview

### 2.1 Layered Architecture Pattern (N-Tier)

The codebase follows a strict layered architecture separating concerns into distinct tiers:

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                           │
│         UI Layer (FormHandlers / REST Resources)                │
│   BailmentOnlyPartsFormHandler, CatalogRestResource            │
├─────────────────────────────────────────────────────────────────┤
│                    BUSINESS LOGIC LAYER                         │
│              Service Layer (Business Logic)                     │
│  BailmentOnlyPartsService, ConsignmentOrderService, AzureBlobService │
├─────────────────────────────────────────────────────────────────┤
│                    APPLICATION LAYER                            │
│                Tools / Helper Layer                             │
│  BailmentOnlyPartsTools, ServicehubRepositoryTool, ConsignmentHelper │
├─────────────────────────────────────────────────────────────────┤
│                    DATA ACCESS LAYER                            │
│              Repository / DAO / CRUD Layer                      │
│  BinMapUploadCRUDOperation, CustomerContractCRUD               │
├─────────────────────────────────────────────────────────────────┤
│                    INFRASTRUCTURE LAYER                         │
│           Database / External Services / Cloud                  │
│  Azure Blob Storage, Azure Key Vault, IBM WMQ, Oracle Database │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Code Flow Example

```java
// STEP 1: UI Layer - BailmentOnlyPartsFormHandler.java
public boolean handleSearch(DynamoHttpServletRequest pRequest, 
        DynamoHttpServletResponse pResponse) {
    // Parse request and delegate to service layer
    List<BailmentOnlyPartResponse> results = bailmentOnlyPartsService.searchParts(
        customerNumber, customerPartNumber, partStatus);
    setResp(new ArrayList<>(results));
}

// STEP 2: Service Layer - BailmentOnlyPartsService.java
public List<BailmentOnlyPartResponse> searchParts(String customerNumber, 
        String customerPartNumber, String partStatus) {
    // Business logic orchestration - delegates to tools layer
    List<RepositoryItem> items = bailmentOnlyPartsTools.queryBailmentOnlyParts(
        customerNumber, customerPartNumber, partStatus);
    return bailmentOnlyPartsTools.populateResponseList(items);
}

// STEP 3: Tools/Repository Layer - BailmentOnlyPartsTools.java
public List<RepositoryItem> queryBailmentOnlyParts(String customerNumber, 
        String customerPartNumber, String partStatus) {
    // Direct database access
    RepositoryView view = inventoryRepository.getView(ITEM_BAILMENT_ONLY_PARTS);
    Builder builder = (Builder) view.getQueryBuilder();
    // Build and execute SQL query
    items = view.executeQuery(builder.createSqlPassthroughQuery(query, null));
    return items == null ? new ArrayList<>() : Arrays.asList(items);
}
```

---

## 3. Creational Patterns

### 3.1 Singleton Pattern

**Purpose:** Ensure a class has only one instance and provide a global point of access.

**Implementation:** All ATG GenericService components are singletons by default (managed by Nucleus container).

```java
// AzureKeyVaultService.java - Singleton via ATG Nucleus
public class AzureKeyVaultService extends GenericService {
    
    // Single instance maintained by Nucleus
    private SecretClient secretClient;
    
    // Thread-safe cache - single instance
    private final ConcurrentHashMap<String, String> secretCache = new ConcurrentHashMap<>();
    
    @Override
    public void doStartService() throws ServiceException {
        super.doStartService();
        // Initialize once when component starts
        secretClient = new SecretClientBuilder()
            .vaultUrl(getVaultUrl())
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        vlogInfo("AzureKeyVaultService initialized with vault: {0}", getVaultUrl());
    }
    
    public String getSecret(String secretName) {
        // Use cached singleton client
        return secretCache.computeIfAbsent(secretName, this::fetchSecret);
    }
}
```

**Configuration (Singleton registration):**
```properties
# AzureKeyVaultService.properties
$class=com.bdsi.service.security.AzureKeyVaultService
$scope=global
vaultUrl=https://bdsi-keyvault.vault.azure.net/
enabled=true
```

---

### 3.2 Factory Pattern

**Purpose:** Create objects without exposing instantiation logic.

**Implementation 1: ATG Nucleus as Factory**
```java
// BinTransactionProcessor.java - Using Nucleus as factory
public void performTask() {
    // Nucleus acts as a factory - resolves and creates component instances
    String processorPath = (String) requestType.getPropertyValue("processor");
    
    // Factory method - creates/retrieves processor dynamically
    Object processor = nucleus.resolveName(processorPath);
    
    if (processor instanceof IScanTransactionProcessor) {
        ((IScanTransactionProcessor) processor).process(uniqueScanCodes);
    }
}
```

**Implementation 2: Builder as Factory**
```java
// AzureBlobService.java - Factory-like client creation with caching
public class AzureBlobService extends GenericService {
    
    // Client cache
    private Map<String, BlobContainerClient> blobContainerClientMap = new HashMap<>();
    
    // Factory method with caching
    private BlobContainerClient getBlobContaineClient(String serviceConfigName) 
            throws GeneralSecurityException, SHServiceException {
        
        // Return cached instance if exists
        if (!CollectionUtils.isEmpty(blobContainerClientMap) 
            && blobContainerClientMap.get(serviceConfigName) != null) {
            return blobContainerClientMap.get(serviceConfigName);
        }
        
        // Get configuration
        AzureClientConfig clientConfig = configurations.getClientConfigs().get(serviceConfigName);
        if (clientConfig == null) {
            throw new SHServiceException("No azure configurations for service :: " + serviceConfigName, "");
        }
        
        // Factory creates new client
        DefaultAzureCredentialBuilder credentialBuilder = new DefaultAzureCredentialBuilder();
        
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint(clientConfig.getServiceEndpoint())
            .credential(credentialBuilder.build())
            .buildClient();
        
        BlobContainerClient blobContainerClient = blobServiceClient
            .getBlobContainerClient(clientConfig.getBlobContainerName());
        
        // Cache for reuse
        blobContainerClientMap.put(serviceConfigName, blobContainerClient);
        return blobContainerClient;
    }
}
```

---

### 3.3 Builder Pattern

**Purpose:** Construct complex objects step by step.

**Implementation 1: Azure SDK Builder**
```java
// AzureBlobService.java - Using Azure SDK Builder
BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
    .endpoint(clientConfig.getServiceEndpoint())
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

**Implementation 2: JWT Token Builder**
```java
// JwtHelper.java - Using JJWT Builder
public String createJwtToken(String subject, long expiryTimeInMin) 
        throws InvalidKeyException, GeneralSecurityException {
    
    Instant currentTime = Instant.now();
    
    return Jwts.builder()
        .setSubject(subject)                                    // Step 1
        .setIssuedAt(Date.from(currentTime))                   // Step 2
        .setExpiration(Date.from(currentTime.plus(             // Step 3
            Duration.ofMinutes(expiryTimeInMin))))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)   // Step 4
        .compact();                                             // Build final token
}
```

**Implementation 3: SQL Query Builder**
```java
// BailmentOnlyPartsTools.java - StringBuilder for SQL queries
public List<RepositoryItem> queryBailmentOnlyParts(String customerNumber, 
        String customerPartNumber, String partStatus) {
    
    StringBuilder queryBuilder = new StringBuilder();
    
    // Step-by-step query construction
    queryBuilder.append("SELECT * FROM sh_bailment_only_parts WHERE customer_number = '")
        .append(customerNumber.toUpperCase())
        .append("'");
    
    if (StringUtils.isNotBlank(customerPartNumber)) {
        queryBuilder.append(" AND UPPER(customer_part_number) LIKE '%")
            .append(customerPartNumber.toUpperCase())
            .append("%'");
    }
    
    if (StringUtils.isBlank(partStatus) || "ACTIVE".equalsIgnoreCase(partStatus)) {
        queryBuilder.append(" AND is_active = 1");
    } else if ("INACTIVE".equalsIgnoreCase(partStatus)) {
        queryBuilder.append(" AND is_active = 0");
    }
    
    queryBuilder.append(" ORDER BY customer_part_number ASC");
    
    String query = queryBuilder.toString();
    // Execute built query
}
```

---

## 4. Structural Patterns

### 4.1 Adapter Pattern

**Purpose:** Convert the interface of a class into another interface clients expect.

```java
// OnHandInventoryCacheAdapter.java - Adapts CacheAdapter to InventoryHelper
public class OnHandInventoryCacheAdapter extends GenericService implements CacheAdapter {
    
    // Adaptee - the actual service being adapted
    private ConsignmentInventoryHelper consignmentInventoryHelper;
    
    @Override
    public Object getCacheElement(Object paramObject) throws Exception {
        long startTime = System.currentTimeMillis();
        vlogInfo("START : getCacheElement{0}", paramObject);
        
        Object result = null;
        
        // Adapt cache key to inventory helper method calls
        if (paramObject instanceof String) {
            String[] params = ((String) paramObject).split("~");
            String screenName = params[0];
            String[] shipToNoArray = params[1].split(",");
            String partNumber = StringUtils.isNotBlank(params[2]) ? params[2].toUpperCase() : null;
            
            // Adapts CacheAdapter interface to ConsignmentInventoryHelper methods
            if ("PART_DETAILS".equalsIgnoreCase(screenName)) {
                result = consignmentInventoryHelper.getBatchOrPartDetails(
                    null, shipToNoArray, partNumber, discrepancy);
            } else if ("BATCH_DETAILS".equalsIgnoreCase(screenName)) {
                String batchNumber = params[4];
                result = consignmentInventoryHelper.getBatchOrPartDetails(
                    batchNumber, shipToNoArray, partNumber, discrepancy);
            } else if ("MAIN_SCREEN".equalsIgnoreCase(screenName)) {
                result = consignmentInventoryHelper.queryInventoryOnHand(
                    shipToNoArray, partNumber, status, batchNumber, ...);
            }
        }
        
        return result;
    }
    
    @Override
    public Object[] getCacheElements(Object[] paramArrayOfObject) throws Exception {
        List<Object> datas = new ArrayList<Object>();
        for(Object key : paramArrayOfObject) {
            datas.add(getCacheElement(key));  // Reuse single element adapter
        }
        return datas.toArray();
    }
}
```

---

### 4.2 Facade Pattern

**Purpose:** Provide a unified interface to a set of interfaces in a subsystem.

```java
// AzureBlobService.java - Facade for Azure Blob Storage operations
public class AzureBlobService extends GenericService {
    
    // Internal components hidden behind facade
    private AzureConfigurations configurations;
    private SecurityUtils securityUtils;
    private AzureKeyVaultService azureKeyVaultService;
    private Map<String, BlobContainerClient> blobContainerClientMap = new HashMap<>();
    
    /**
     * FACADE METHOD 1: Simple file upload
     * Hides complexity of: client creation, path normalization, stream handling
     */
    public void pushFile(String filePathFrom, String filePathTo, String serviceName) 
            throws SHServiceException {
        try {
            BlobContainerClient containerClient = getBlobContaineClient(
                StringUtils.isNotBlank(serviceName) ? serviceName : serviceConfigName);
            
            // Path normalization (hidden complexity)
            if (filePathTo.startsWith("/")) {
                filePathTo = filePathTo.substring(1);
            }
            
            File sourceFile = new File(filePathFrom);
            String fileName = sourceFile.getName();
            
            BlobClient blobClient = containerClient.getBlobClient(filePathTo);
            
            // Stream handling (hidden complexity)
            if (uploadFromFileStream) {
                uploadFromFileStream(blobClient, sourceFile);
            } else {
                blobClient.uploadFromFile(filePathFrom, true);
            }
        } catch(Exception e) {
            throw new SHServiceException("Exception in AzureBlobService.pushFile", "", e);
        }
    }
    
    /**
     * FACADE METHOD 2: Zip and upload
     * Hides complexity of: on-the-fly compression, streaming to Azure
     */
    public String pushZipFile(String filePathFrom, String filePathTo, 
            String fileName, String serviceName) throws SHServiceException {
        
        String zipFilePath = null;
        try {
            BlobContainerClient containerClient = getBlobContaineClient(serviceName);
            zipFilePath = getZipFilePath(filePathFrom, filePathTo, fileName);
            
            File localFile = new File(filePathFrom);
            BlockBlobClient zipBlobClient = containerClient
                .getBlobClient(zipFilePath.trim())
                .getBlockBlobClient();
            
            // Complex streaming + compression (hidden behind facade)
            try (OutputStream azureOutputStream = zipBlobClient.getBlobOutputStream(true);
                 ZipOutputStream zipOutputStream = new ZipOutputStream(azureOutputStream);
                 FileInputStream fileInputStream = new FileInputStream(localFile)) {
                
                zipOutputStream.putNextEntry(new ZipEntry(localFile.getName()));
                byte[] buffer = new byte[8192];
                int length;
                while ((length = fileInputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, length);
                }
                zipOutputStream.closeEntry();
            }
        } catch (Exception e) {
            throw new SHServiceException("Exception in pushZipFile", "", e);
        }
        return zipFilePath;
    }
}
```

---

### 4.3 Dependency Injection (DI) / Inversion of Control (IoC)

**Purpose:** Remove hard-coded dependencies and make it possible to change them at runtime or compile time.

```java
// BailmentOnlyPartsTools.java - Dependencies injected via ATG Nucleus
public class BailmentOnlyPartsTools extends GenericService {
    
    // All dependencies injected - no direct instantiation
    @Getter @Setter
    private Repository userProfileRepository;      // Injected
    
    @Getter @Setter
    private Repository inventoryRepository;        // Injected
    
    @Getter @Setter
    private Repository customerContractRepository; // Injected
    
    @Getter @Setter
    private FileValidationService fileValidationService;  // Injected
    
    @Getter @Setter
    private CustomerContractCRUD customerContractCRUD;    // Injected
    
    @Getter @Setter
    private AuditTrailTools auditTrailTools;              // Injected
    
    @Getter @Setter
    private RqlStatement bailmentOnlyByCustomerRql;       // Injected
    
    // Methods use injected dependencies - not creating them
    public boolean isPartInCMIR(Map<String, Object> dataMap) {
        // Uses injected customerContractCRUD
        RepositoryItem[] cmirItems = customerContractCRUD
            .getCMIRDataUsingCustomerAndPrimePN(customerNumber, primePartNumber, customerPartNumber);
        // ...
    }
}
```

**Configuration (Properties File):**
```properties
# BailmentOnlyPartsTools.properties
$class=com.bdsi.servicehub.bailmentonly.services.BailmentOnlyPartsTools

# Dependency Injection Configuration
userProfileRepository=/atg/userprofiling/ProfileAdapterRepository
inventoryRepository=/atg/commerce/inventory/InventoryRepositoryExtension
customerContractRepository=/com/bdsi/repository/CustomerContractRepository
fileValidationService=/com/bdsi/servicehub/services/FileValidationService
customerContractCRUD=/com/bdsi/servicehub/services/CustomerContractCRUD
auditTrailTools=/com/bdsi/servicehub/audit/AuditTrailTools
bailmentOnlyByCustomerRql=customerNumber = ?0
```

---

## 5. Behavioral Patterns

### 5.1 Strategy Pattern

**Purpose:** Define a family of algorithms, encapsulate each one, and make them interchangeable.

**Implementation 1: Scan Transaction Processors**
```java
// IScanTransactionProcessor.java - Strategy Interface
public interface IScanTransactionProcessor {
    
    // Algorithm method - different implementations process differently
    void process(ConcurrentLinkedQueue<ScanTransactionRequest> scanRequest) 
        throws SHOrderProcessorException;
    
    SHEmailTools getEmailTools();
    
    // Default method for common behavior
    default void handleException(String[] emailRecipients, 
            Map<String, Object> emailParams, String processorName) {
        getEmailTools().sendBinScanFailureEmail(emailRecipients, emailParams, processorName);
    }
}

// BinTransactionProcessor.java - Context that uses Strategy
public class BinTransactionProcessor extends SingletonSchedulableService {
    
    public void performTask() {
        txnIdToDetailMap.entrySet().parallelStream().forEach(entry -> {
            RepositoryItem reqeustItem = entry.getKey();
            RepositoryItem requestType = (RepositoryItem) reqeustItem.getPropertyValue("requestType");
            
            // Get processor path dynamically
            String processorPath = (String) requestType.getPropertyValue("processor");
            
            // Strategy Pattern: Resolve concrete strategy at runtime
            Object processor = nucleus.resolveName(processorPath);
            
            if (processor instanceof IScanTransactionProcessor) {
                // Execute strategy's algorithm
                ((IScanTransactionProcessor) processor).process(uniqueScanCodes);
                ((IScanTransactionProcessor) processor).process(reqList);
            }
        });
    }
}
```

**Implementation 2: File Reader Strategy**
```java
// FileReader.java - Strategy Interface
public interface FileReader {
    List<FileMappingRecord> parseFile(File file, FileMappingBean fileMapping) 
        throws FileMappingFeedException;
}

// CSVFileReader.java - Concrete Strategy for CSV files
public class CSVFileReader implements FileReader {
    @Override
    public List<FileMappingRecord> parseFile(File file, FileMappingBean fileMapping) {
        // CSV-specific parsing logic
    }
}

// TextFileReader.java - Concrete Strategy for text files
public class TextFileReader implements FileReader {
    @Override
    public List<FileMappingRecord> parseFile(File file, FileMappingBean fileMapping) {
        // Text file specific parsing logic
    }
}
```

**Implementation 3: Feed Processor Strategy**
```java
// FeedProcessor.java - Strategy Interface
public interface FeedProcessor {
    void process(String filePath, String fileName) throws ExternalFileProcessorException;
    List<String> validateFiles(String sourceDir, List<String> fileNames);
}

// Multiple concrete strategies:
// - AsnFileProcessor
// - ChgFileProcessor
// - StrFileProcessor
// - XmlFileProcessor
// - ForecastFileProcessor
```

---

### 5.2 Template Method Pattern

**Purpose:** Define the skeleton of an algorithm in the superclass but let subclasses override specific steps.

```java
// SingletonSchedulableService (ATG Framework) - Template class
// BinTransactionProcessor.java - Concrete implementation
public class BinTransactionProcessor extends SingletonSchedulableService {
    
    @Getter @Setter
    private boolean enabled;
    
    private volatile ForkJoinPool customThreadPool;
    private volatile boolean isRunning = false;
    
    /**
     * TEMPLATE METHOD - defined in parent class
     * Called by scheduler framework at configured intervals
     */
    @Override
    public void doScheduledTask(Scheduler arg0, ScheduledJob arg1) {
        if (isEnabled()) {
            performTask();  // Hook method - implemented here
        } else {
            vlogInfo("Bin transaction scheduler is disabled");
        }
    }
    
    /**
     * HOOK METHOD - subclass-specific implementation
     */
    public void performTask() {
        // Skip if previous run still active
        if (isRunning) {
            vlogInfo("Previous run still active, skipping this cycle");
            return;
        }
        
        // Lazily create thread pool
        if (customThreadPool == null || customThreadPool.isShutdown()) {
            customThreadPool = new ForkJoinPool(parallelism);
        }
        
        isRunning = true;
        try {
            customThreadPool.submit(() -> {
                try {
                    // Actual processing logic
                    RepositoryView rv = getTxnRepository()
                        .getView(ConstantsUtility.ITEM_TYPE_BINORDERREQUESTDETAIL);
                    RepositoryItem[] currentDatas = getDataQuery().executeQuery(rv, null);
                    
                    if (currentDatas != null) {
                        processTransactionData(currentDatas);
                    }
                } finally {
                    isRunning = false;
                }
            });
        } catch (Exception e) {
            isRunning = false;
            vlogError(e, "Failed to submit task to pool");
        }
    }
}
```

---

### 5.3 Command Pattern

**Purpose:** Encapsulate a request as an object, allowing parameterization of clients with queues, requests, and operations.

```java
// BailmentOnlyPartsFormHandler.java - Each handle* method is a Command
public class BailmentOnlyPartsFormHandler extends ServiceHubRequestHandler {
    
    @Getter @Setter
    private BailmentOnlyPartsService bailmentOnlyPartsService;
    
    @Getter @Setter
    private BailmentOnlyPartsTools bailmentOnlyPartsTools;
    
    /**
     * COMMAND 1: Download blank template
     */
    public boolean handleDownloadTemplate(DynamoHttpServletRequest pRequest, 
            DynamoHttpServletResponse pResponse) throws ServletException, IOException {
        byte[] templateBytes = bailmentOnlyPartsService.getBlankTemplate();
        // Set response headers and write file
        return true;
    }
    
    /**
     * COMMAND 2: Download data template with existing data
     */
    public boolean handleDownloadDataTemplate(DynamoHttpServletRequest pRequest, 
            DynamoHttpServletResponse pResponse) throws ServletException, IOException {
        String customerNumbers = requestMap.get("customerNumbers");
        byte[] templateBytes = bailmentOnlyPartsService.getDataTemplate(customerNumbers);
        // Set response and write
        return true;
    }
    
    /**
     * COMMAND 3: Search parts
     */
    public boolean handleSearch(DynamoHttpServletRequest pRequest, 
            DynamoHttpServletResponse pResponse) throws ServletException, IOException {
        List<BailmentOnlyPartResponse> results = bailmentOnlyPartsService.searchParts(
            customerNumber, customerPartNumber, partStatus);
        setResp(new ArrayList<>(results));
        return checkFormRedirect(getSuccessURL(), getErrorURL(), pRequest, pResponse);
    }
    
    /**
     * COMMAND 4: Add new part
     */
    public boolean handleAdd(DynamoHttpServletRequest pRequest, 
            DynamoHttpServletResponse pResponse) throws ServletException, IOException {
        // Validate and add part
        RepositoryItem item = bailmentOnlyPartsTools.addBailmentOnlyPart(
            customerNumber, customerPartNumber, primePartNumber, dataMap);
        return checkFormRedirect(getSuccessURL(), getErrorURL(), pRequest, pResponse);
    }
    
    /**
     * COMMAND 5: Update existing part
     */
    public boolean handleUpdate(DynamoHttpServletRequest pRequest, 
            DynamoHttpServletResponse pResponse) throws ServletException, IOException {
        RepositoryItem item = bailmentOnlyPartsTools.updateBailmentOnlyPart(
            customerNumber, customerPartNumber, primePartNumber, dataMap);
        return checkFormRedirect(getSuccessURL(), getErrorURL(), pRequest, pResponse);
    }
    
    /**
     * COMMAND 6: Delete (deactivate) part
     */
    public boolean handleDelete(DynamoHttpServletRequest pRequest, 
            DynamoHttpServletResponse pResponse) throws ServletException, IOException {
        boolean deleted = bailmentOnlyPartsTools.deleteBailmentOnlyPart(
            customerNumber, customerPartNumber, primePartNumber);
        return checkFormRedirect(getSuccessURL(), getErrorURL(), pRequest, pResponse);
    }
    
    /**
     * COMMAND 7: Initiate file upload
     */
    public boolean handleInitiateUpload(DynamoHttpServletRequest pRequest, 
            DynamoHttpServletResponse pResponse) throws ServletException, IOException {
        Workbook workbook = getWorkBook(getUploadProperty());
        List<BailmentOnlyUploadRow> uploadRows = parseExcelFile(workbook);
        // Validate and store in memory
        return checkFormRedirect(getSuccessURL(), getErrorURL(), pRequest, pResponse);
    }
    
    /**
     * COMMAND 8: Save uploaded data
     */
    public boolean handleSaveUpload(DynamoHttpServletRequest pRequest, 
            DynamoHttpServletResponse pResponse) throws ServletException, IOException {
        List<BailmentOnlyUploadRow> uploadRows = getUploadDataMap().remove(requestId);
        // Process and save each row
        return checkFormRedirect(getSuccessURL(), getErrorURL(), pRequest, pResponse);
    }
}
```

---

### 5.4 Observer Pattern (Message-Driven)

**Purpose:** Define a one-to-many dependency so when one object changes state, dependents are notified.

```java
// WMQMessagingManager.java - Observer/Event processing via message queues
public class WMQMessagingManager extends SingletonSchedulableService {
    
    // Array of message sink connectors (observers)
    private WMQSinkConnector[] sinkConenctor = null;
    private boolean enabled;
    private boolean asyncChkFlag;
    
    @Override
    public void doScheduledTask(Scheduler var1, ScheduledJob var2) {
        if (isEnabled()) {
            vlogInfo("WMQMessagingManager scheduler is enabled");
            performScheduledTask();
        }
    }
    
    public void performScheduledTask() {
        if (sinkConenctor == null) {
            vlogInfo("No connector found");
            return;
        }
        
        if (isAsyncChkFlag()) {
            // Async notification - observers process messages in parallel
            CompletableFuture.runAsync(() -> {
                Arrays.asList(sinkConenctor).parallelStream().forEach(connector -> {
                    try {
                        vlogDebug("Attempting to connect on queue: {0}", 
                            connector.getContextFactory().getQueueName());
                        connector.processSink();  // Observer receives message
                    } catch (Exception e) {
                        vlogError(e, "Exception in processing sink");
                    }
                });
            });
            vlogInfo("All sink invoked: {0}", Arrays.toString(sinkConenctor));
        } else {
            // Sync notification
            Arrays.asList(sinkConenctor).parallelStream().forEach(connector -> {
                connector.processSink();
            });
        }
    }
}
```

---

## 6. Enterprise Patterns

### 6.1 Repository Pattern (Data Access Object)

**Purpose:** Separate the logic that retrieves data from the business logic.

```java
// BinMapUploadCRUDOperation.java - Repository Pattern
public class BinMapUploadCRUDOperation extends GenericService {
    
    @Getter @Setter
    private Repository userProfileRepository;
    @Getter @Setter
    private Repository inventoryRepository;
    @Getter @Setter
    private RqlStatement scanCodequery;
    
    /**
     * READ operation - Find by scan code
     */
    public RepositoryItem viewBinMapSetup(String scanCode) throws RepositoryException {
        RepositoryView binView = getInventoryRepository().getView("invBin");
        Object[] params = new Object[] { scanCode };
        RepositoryItem[] binItems = getScanCodequery().executeQuery(binView, params);
        if (null != binItems) {
            return binItems[0];
        }
        return null;
    }
    
    /**
     * CREATE/UPDATE operation - Save bin map upload
     */
    public Map<String, List<String>> savebinMapUpload(List<BinUploadRow> dataRows, 
            String updatedBy) throws SHServiceException {
        
        Map<String, List<String>> updatedBinsMap = new HashMap<>();
        TransactionManager tm = ((RepositoryImpl) getUserProfileRepository()).getTransactionManager();
        TransactionDemarcation td = new TransactionDemarcation();
        boolean isRollback = false;
        
        try {
            td.begin(tm, TransactionDemarcation.REQUIRED);
            MutableRepository mInvRepo = (MutableRepository) getInventoryRepository();
            
            for (BinUploadRow dataRow : dataRows) {
                // Batch transaction management
                if (i % recordsPerTransaction == 0) {
                    td.end(isRollback);
                    td = new TransactionDemarcation();
                    td.begin(tm, TransactionDemarcation.REQUIRED);
                }
                
                // Create or Update logic
                addOrUpdateBin(dataRow);
                i++;
            }
        } catch (Exception e) {
            isRollback = true;
            throw new SHServiceException(e);
        } finally {
            td.end(isRollback);
        }
        return updatedBinsMap;
    }
    
    /**
     * READ operation - Get all UOMs
     */
    public RepositoryItem[] showAllUOM() throws RepositoryException {
        RepositoryView uomView = inventoryRepository.getView("uom");
        return getAllRql().executeQuery(uomView, null);
    }
    
    /**
     * READ operation - Get all ASLs
     */
    public RepositoryItem[] showAllAsls() throws RepositoryException {
        RepositoryView aslView = inventoryRepository.getView("asl");
        return getAllRql().executeQuery(aslView, null);
    }
}
```

---

### 6.2 Data Transfer Object (DTO) Pattern

**Purpose:** Transfer data between layers without exposing internal structures.

```java
// BailmentOnlyPartResponse.java - DTO for API responses
@Getter
@Setter
public class BailmentOnlyPartResponse {
    
    private String id;
    private String customerNumber;
    private String customerName;
    private String customerPartNumber;
    private String primePartNumber;
    private String invoicedPartNumber;
    private String uom;
    private String isActive;
    private String lastUpdatedTime;
    private int version;
    private boolean hardStop = false;
    
    // Validation messages
    private List<Message> messages = new ArrayList<>();
}

// BailmentOnlyUploadRow.java - DTO for file upload processing
@Getter
@Setter
public class BailmentOnlyUploadRow {
    
    private String customerNumber;
    private String customerPartNumber;
    private String primePartNumber;
    private String invoicedPartNumber;
    private String uom;
    private String isActive;
    private int rowNumber;
    private BailmentOnlyPartResponse response;
    
    /**
     * Convert DTO to Map for repository operations
     */
    public Map<String, Object> toDataMap() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("customerNumber", customerNumber);
        dataMap.put("customerPartNumber", customerPartNumber);
        dataMap.put("primePartNumber", primePartNumber);
        dataMap.put("invoicedPartNumber", invoicedPartNumber);
        dataMap.put("uom", uom);
        dataMap.put("isActive", isActive);
        return dataMap;
    }
    
    /**
     * Check if row is empty (skip processing)
     */
    public boolean isEmpty() {
        return StringUtils.isBlank(customerNumber) 
            && StringUtils.isBlank(customerPartNumber)
            && StringUtils.isBlank(primePartNumber);
    }
    
    /**
     * Check for validation errors
     */
    public boolean hasHardStopError() {
        return response != null && response.isHardStop();
    }
}

// Message.java - Nested DTO for validation messages
@Getter
@Setter
public class Message {
    private String messageKey;
    private String message;
    private boolean hardStop = true;
}
```

---

### 6.3 Transaction Script Pattern

**Purpose:** Organize business logic by procedures where each procedure handles a single request.

```java
// ConsignmentOrderManager.java - Transaction Script Pattern
public class ConsignmentOrderManager extends GenericService {
    
    @Getter @Setter
    private KLXOrderManager orderManager;
    @Getter @Setter
    private MutableRepository orderRepository;
    
    /**
     * Transaction Script: Remove Order
     * Complete transaction for removing an order
     */
    public void removeOrder(String atgOrderId) {
        vlogDebug("START : removeOrder with Id : {0}", atgOrderId);
        
        TransactionManager tm = ((RepositoryImpl) orderRepository).getTransactionManager();
        TransactionDemarcation td = new TransactionDemarcation();
        boolean success = false;
        
        try {
            // Begin transaction
            td.begin(tm, TransactionDemarcation.REQUIRED);
            
            // Execute business logic
            getOrderManager().removeOrder(atgOrderId);
            
            success = true;
            vlogDebug("Order {0} removed successfully", atgOrderId);
            
        } catch (TransactionDemarcationException e) {
            vlogError(e, "TransactionDemarcationException");
        } catch (CommerceException e) {
            vlogError(e, "CommerceException");
        } finally {
            // End transaction - rollback if not successful
            if (td != null) {
                try {
                    td.end(!success);
                } catch (TransactionDemarcationException e) {
                    vlogError(e, "Error ending transaction");
                }
            }
        }
    }
    
    /**
     * Transaction Script: Update Order
     * Complete transaction for updating order with synchronized block
     */
    public void updateOrder(KLXOrder order, String commerceItemId) {
        vlogDebug("START : updateOrder");
        
        TransactionManager tm = ((RepositoryImpl) orderRepository).getTransactionManager();
        TransactionDemarcation td = new TransactionDemarcation();
        CommerceItemManager cm = orderManager.getCommerceItemManager();
        boolean isRollback = false;
        
        // Synchronized on order to prevent concurrent modifications
        synchronized (order) {
            try {
                td.begin(tm, TransactionDemarcation.REQUIRED);
                
                // Business logic
                cm.removeItemFromOrder(order, commerceItemId);
                getOrderManager().updateOrder(order);
                
            } catch (Exception e) {
                isRollback = true;
                vlogError(e, "Exception updating order");
            } finally {
                try {
                    td.end(isRollback);
                } catch (TransactionDemarcationException e) {
                    vlogError(e, "Error ending transaction");
                }
            }
        }
    }
}
```

---

### 6.4 Unit of Work Pattern

**Purpose:** Maintain a list of objects affected by a business transaction and coordinate writing out changes.

```java
// BinMapUploadCRUDOperation.java - Unit of Work with batch commits
public Map<String, List<String>> savebinMapUpload(List<BinUploadRow> dataRows, 
        String updatedBy) throws SHServiceException {
    
    Map<String, List<String>> updatedBinsMap = new HashMap<>();
    TransactionManager tm = ((RepositoryImpl) getUserProfileRepository()).getTransactionManager();
    TransactionDemarcation td = new TransactionDemarcation();
    boolean isRollback = false;
    int i = 1;
    
    try {
        // Start Unit of Work
        td.begin(tm, TransactionDemarcation.REQUIRED);
        MutableRepository mInvRepo = (MutableRepository) getInventoryRepository();
        
        for (BinUploadRow dataRow : dataRows) {
            
            // Batch commit - commit every N records
            if (i % recordsPerTransaction == 0) {
                try {
                    td.end(isRollback);  // Commit current batch
                    vlogDebug("Committed batch at record {0}", i);
                } catch (TransactionDemarcationException e) {
                    vlogError(e, "Error committing batch");
                }
                
                // Start new Unit of Work
                td = new TransactionDemarcation();
                td.begin(tm, TransactionDemarcation.REQUIRED);
                vlogDebug("Started new transaction at record {0}", i);
            }
            
            // Track changes in this unit of work
            boolean created = addOrUpdateBin(dataRow);
            
            // Track what was created/updated
            if (created) {
                if (updatedBinsMap.containsKey(dataRow.getBin().getSapBpNum())) {
                    updatedBinsMap.get(dataRow.getBin().getSapBpNum())
                        .add(dataRow.getBin().getScanCode() + "~" + CREATED_STATUS);
                } else {
                    updatedBinsMap.put(dataRow.getBin().getSapBpNum(), new ArrayList<>());
                    updatedBinsMap.get(dataRow.getBin().getSapBpNum())
                        .add(dataRow.getBin().getScanCode() + "~" + CREATED_STATUS);
                }
            }
            i++;
        }
    } catch (Exception e) {
        isRollback = true;
        vlogError(e, "Exception in savebinMapUpload");
        throw new SHServiceException(e);
    } finally {
        // End final Unit of Work
        try {
            if (tm != null) {
                td.end(isRollback);
            }
            vlogDebug("Final transaction ended. Total records: {0}", i);
        } catch (TransactionDemarcationException e) {
            vlogError(e, "Error ending final transaction");
        }
    }
    return updatedBinsMap;
}
```

---

## 7. Microservices Patterns

### 7.1 Filter / Interceptor Pattern

**Purpose:** Intercept requests/responses for cross-cutting concerns like security, logging.

```java
// BDSICsrfProtectionFilter.java - Security Filter
public class BDSICsrfProtectionFilter extends GenericService implements RestRequestValidator {
    
    // Methods that don't need CSRF protection
    private static final Set<String> METHODS_TO_IGNORE;
    static {
        HashSet<String> mti = new HashSet<String>();
        mti.add("GET");
        mti.add("OPTIONS");
        mti.add("HEAD");
        METHODS_TO_IGNORE = Collections.unmodifiableSet(mti);
    }
    
    private String headerKey;
    private boolean enabled;
    
    /**
     * Intercept and validate every REST request
     */
    public void validate() throws RestException {
        if (isEnabled()) {
            RestContext context = RestContext.getCurrentContext();
            ContainerRequestContext reqContext = context.getRequestContext();
            
            // Skip safe methods
            if (!METHODS_TO_IGNORE.contains(reqContext.getMethod()) 
                && !reqContext.getHeaders().containsKey(getHeaderKey())) {
                
                vlogWarning("CSRF vulnerability found - security threat occurred");
                RestException error = new RestException("CSRF Error");
                error.setStatusCode(400);
                throw error;
            }
        }
    }
}
```

---

### 7.2 REST Resource Pattern (JAX-RS)

**Purpose:** Expose business functionality as REST endpoints.

```java
// CatalogRestResource.java - RESTful API Resource
@RestResource(id = "com.bdsi.servicehub.catalog.restresources")
@Api(value = "Catalog")
@Path("/cmp/catalog")
public class CatalogRestResource extends GenericService {
    
    @Getter @Setter
    private KLXCatalogTools catalogTools;
    
    /**
     * GET /cmp/catalog/{searchKey}
     * Retrieves catalog help text data
     */
    @GET
    @Path(value = "/{searchKey}")
    @Endpoint(id = "/cmp/catalog/{searchKey}#GET", isSingular = true, filterId = "catalog.response")
    @ApiOperation(value = "Get Catalog Help Text details", response = AuditTrailResponse.class)
    public Response getHelpTextDetails(
            @ApiParam(required = true) @PathParam(value = "searchKey") String searchKey) 
            throws RestException {
        
        long startTime = System.currentTimeMillis();
        try {
            // Delegate to service layer
            List<MediaContentVO> mediaContentList = catalogTools.getMediaData(searchKey);
            
            // Build standardized response
            return JaxrsUtil.build(mediaContentList, Status.OK, "Data retrieved successfully");
            
        } catch (Exception e) {
            vlogError(e, "Error fetching help text for key {0}", searchKey);
            
            // Standardized error response
            Message msg = new Message("ERR500", e.getMessage());
            return JaxrsUtil.build(null, Status.INTERNAL_SERVER_ERROR, 
                "Error fetching help text data", Message.getList(msg));
        } finally {
            vlogDebug("Request processed in {0}ms", System.currentTimeMillis() - startTime);
        }
    }
}
```

---

### 7.3 Caching Pattern

**Purpose:** Store data temporarily for faster access.

```java
// AzureKeyVaultService.java - In-Memory Caching
public class AzureKeyVaultService extends GenericService {
    
    private SecretClient secretClient;
    
    // Thread-safe cache using ConcurrentHashMap
    private final ConcurrentHashMap<String, String> secretCache = new ConcurrentHashMap<>();
    
    /**
     * Get secret with caching
     * Uses computeIfAbsent for thread-safe lazy loading
     */
    public String getSecret(String secretName) {
        return secretCache.computeIfAbsent(secretName, this::fetchSecret);
    }
    
    /**
     * Set secret and update cache
     */
    public void setSecret(String secretName, String secretValue) {
        secretClient.setSecret(secretName, secretValue);
        secretCache.put(secretName, secretValue);
        vlogInfo("Secret pushed to Key Vault: {0}", secretName);
    }
    
    /**
     * Clear cache when needed
     */
    public void clearCache() {
        secretCache.clear();
        vlogInfo("AzureKeyVaultService secret cache cleared");
    }
    
    /**
     * Fetch from actual vault (cache miss)
     */
    private String fetchSecret(String secretName) {
        vlogDebug("Fetching secret from Key Vault: {0}", secretName);
        return secretClient.getSecret(secretName).getValue();
    }
}

// AzureBlobService.java - Client Caching
public class AzureBlobService extends GenericService {
    
    // Cache blob container clients to avoid repeated creation
    private Map<String, BlobContainerClient> blobContainerClientMap = new HashMap<>();
    
    private BlobContainerClient getBlobContaineClient(String serviceConfigName) {
        // Check cache first
        if (!CollectionUtils.isEmpty(blobContainerClientMap) 
            && blobContainerClientMap.get(serviceConfigName) != null) {
            return blobContainerClientMap.get(serviceConfigName);  // Cache hit
        }
        
        // Cache miss - create and cache
        BlobContainerClient client = createNewClient(serviceConfigName);
        blobContainerClientMap.put(serviceConfigName, client);
        return client;
    }
}
```

---

### 7.4 Scheduler Pattern

**Purpose:** Execute tasks at scheduled intervals.

```java
// BinTransactionProcessor.java - Scheduled Task Processing
public class BinTransactionProcessor extends SingletonSchedulableService {
    
    @Getter @Setter
    private boolean enabled;
    
    @Getter @Setter
    private int parallelism;
    
    @Getter @Setter
    private BinTransactionManager binTransactionManager;
    
    // Shared thread pool - prevents thread leak
    private volatile ForkJoinPool customThreadPool;
    
    // Guard against overlapping runs
    private volatile boolean isRunning = false;
    
    /**
     * Called by ATG Scheduler at configured intervals
     */
    @Override
    public void doScheduledTask(Scheduler arg0, ScheduledJob arg1) {
        if (isEnabled()) {
            performTask();
        } else {
            vlogInfo("Bin transaction scheduler is disabled");
        }
    }
    
    /**
     * Actual task execution with thread safety
     */
    public void performTask() {
        // Prevent overlapping runs
        if (isRunning) {
            vlogInfo("Previous run still active, skipping this cycle");
            return;
        }
        
        // Lazily create or recreate thread pool
        if (customThreadPool == null || customThreadPool.isShutdown()) {
            customThreadPool = new ForkJoinPool(parallelism);
        }
        
        long startTime = System.currentTimeMillis();
        isRunning = true;
        
        try {
            customThreadPool.submit(() -> {
                try {
                    // Process pending transactions
                    RepositoryView rv = getTxnRepository()
                        .getView(ConstantsUtility.ITEM_TYPE_BINORDERREQUESTDETAIL);
                    RepositoryItem[] currentDatas = getDataQuery().executeQuery(rv, null);
                    
                    if (currentDatas != null) {
                        processTransactionData(currentDatas);
                    } else {
                        vlogInfo("No open requests found at {0}", 
                            new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
                                .format(Calendar.getInstance().getTime()));
                    }
                    
                    // Log execution time
                    long timeDiff = System.currentTimeMillis() - startTime;
                    vlogInfo("Processing completed in {0}hr {1}min {2}sec",
                        TimeUnit.MILLISECONDS.toHours(timeDiff),
                        TimeUnit.MILLISECONDS.toMinutes(timeDiff) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(timeDiff) % 60);
                        
                } finally {
                    isRunning = false;
                }
            });
        } catch (Exception e) {
            isRunning = false;
            vlogError(e, "Failed to submit task to pool");
        }
    }
}
```

---

## 8. Code Flow Diagrams

### 8.1 Complete Request Flow

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                           COMPLETE REQUEST FLOW                                  │
├──────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  HTTP Request (POST /rest/cmp/bailmentonly/search)                              │
│       │                                                                          │
│       ▼                                                                          │
│  ┌──────────────────────────┐                                                    │
│  │   Security Filters       │ ───► BDSICsrfProtectionFilter                     │
│  │   (Interceptor Pattern)  │      JWT Validation                               │
│  └────────────┬─────────────┘                                                    │
│               │                                                                  │
│               ▼                                                                  │
│  ┌──────────────────────────┐                                                    │
│  │   REST Resource /        │ ───► CatalogRestResource                          │
│  │   FormHandler            │      BailmentOnlyPartsFormHandler.handleSearch()  │
│  │   (Command Pattern)      │                                                    │
│  └────────────┬─────────────┘                                                    │
│               │                                                                  │
│               ▼                                                                  │
│  ┌──────────────────────────┐                                                    │
│  │   Service Layer          │ ───► BailmentOnlyPartsService.searchParts()       │
│  │   (Facade Pattern)       │      AzureBlobService                             │
│  └────────────┬─────────────┘                                                    │
│               │                                                                  │
│               ▼                                                                  │
│  ┌──────────────────────────┐                                                    │
│  │   Tools / Helper Layer   │ ───► BailmentOnlyPartsTools.queryBailmentOnlyParts│
│  │   (Strategy Pattern)     │      ConsignmentTransactionHelper                 │
│  └────────────┬─────────────┘                                                    │
│               │                                                                  │
│               ▼                                                                  │
│  ┌──────────────────────────┐                                                    │
│  │   Repository / CRUD      │ ───► BinMapUploadCRUDOperation                    │
│  │   (Repository Pattern)   │      CustomerContractCRUD                         │
│  └────────────┬─────────────┘                                                    │
│               │                                                                  │
│               ▼                                                                  │
│  ┌──────────────────────────┐                                                    │
│  │   External Systems       │ ───► Oracle Database                              │
│  │   (Adapter Pattern)      │      Azure Blob Storage                           │
│  │                          │      Azure Key Vault                              │
│  │                          │      IBM WebSphere MQ                             │
│  └──────────────────────────┘                                                    │
│                                                                                  │
└──────────────────────────────────────────────────────────────────────────────────┘
```

### 8.2 File Upload Processing Flow

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                         FILE UPLOAD PROCESSING FLOW                              │
├──────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  1. User uploads Excel file                                                      │
│       │                                                                          │
│       ▼                                                                          │
│  ┌──────────────────────────────┐                                                │
│  │ handleInitiateUpload()       │  ◄── Command Pattern                          │
│  │ - Parse Excel file           │                                                │
│  │ - Create BailmentUploadRow   │  ◄── DTO Pattern                              │
│  │ - Validate each row          │                                                │
│  │ - Store in memory (Map)      │                                                │
│  │ - Return validation results  │                                                │
│  └──────────────┬───────────────┘                                                │
│                 │                                                                │
│                 ▼                                                                │
│  2. User reviews validation results                                              │
│       │                                                                          │
│       ├──── Has Errors? ──► handleDownloadErrorReport()                         │
│       │                      Generate Excel with errors                          │
│       │                                                                          │
│       ▼                                                                          │
│  3. User confirms save                                                           │
│       │                                                                          │
│       ▼                                                                          │
│  ┌──────────────────────────────┐                                                │
│  │ handleSaveUpload()           │  ◄── Command Pattern                          │
│  │ - Retrieve from memory       │                                                │
│  │ - For each valid row:        │                                                │
│  │   - addBailmentOnlyPart()    │  ◄── Repository Pattern                       │
│  │   - updateBailmentOnlyPart() │                                                │
│  │ - Track success/error count  │                                                │
│  │ - Return summary             │                                                │
│  └──────────────────────────────┘                                                │
│                                                                                  │
└──────────────────────────────────────────────────────────────────────────────────┘
```

### 8.3 Scheduled Task Processing Flow

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                      SCHEDULED TASK PROCESSING FLOW                              │
├──────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ATG Scheduler (every N minutes)                                                 │
│       │                                                                          │
│       ▼                                                                          │
│  ┌──────────────────────────────┐                                                │
│  │ doScheduledTask()            │  ◄── Template Method Pattern                  │
│  │ - Check if enabled           │                                                │
│  │ - Call performTask()         │                                                │
│  └──────────────┬───────────────┘                                                │
│                 │                                                                │
│                 ▼                                                                │
│  ┌──────────────────────────────┐                                                │
│  │ performTask()                │                                                │
│  │ - Check if already running   │  ◄── Guard flag prevents overlap              │
│  │ - Create/reuse ForkJoinPool  │                                                │
│  │ - Submit async task          │                                                │
│  └──────────────┬───────────────┘                                                │
│                 │                                                                │
│                 ▼                                                                │
│  ┌──────────────────────────────┐                                                │
│  │ Process in ForkJoinPool      │  ◄── Parallel processing                      │
│  │ - Query pending transactions │                                                │
│  │ - Group by request type      │                                                │
│  │ - Resolve processor (Strategy│  ◄── Strategy Pattern                         │
│  │   Pattern)                   │                                                │
│  │ - Process each request       │                                                │
│  │ - Update status              │                                                │
│  │ - Send notifications         │                                                │
│  └──────────────────────────────┘                                                │
│                                                                                  │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 9. Pattern Summary Table

| # | Pattern | Category | Implementation Class | Purpose |
|---|---------|----------|---------------------|---------|
| 1 | **Layered Architecture** | Architectural | All modules | Separation of concerns |
| 2 | **Singleton** | Creational | GenericService (all components) | Single instance |
| 3 | **Factory** | Creational | Nucleus.resolveName(), BlobServiceClientBuilder | Object creation |
| 4 | **Builder** | Creational | Jwts.builder(), BlobServiceClientBuilder, StringBuilder | Complex object construction |
| 5 | **Adapter** | Structural | OnHandInventoryCacheAdapter | Interface conversion |
| 6 | **Facade** | Structural | AzureBlobService, BailmentOnlyPartsService | Hide complexity |
| 7 | **Dependency Injection** | Structural | All components via .properties files | Loose coupling |
| 8 | **Strategy** | Behavioral | IScanTransactionProcessor, FileReader, FeedProcessor | Interchangeable algorithms |
| 9 | **Template Method** | Behavioral | SingletonSchedulableService | Algorithm skeleton |
| 10 | **Command** | Behavioral | FormHandler handle* methods | Request encapsulation |
| 11 | **Observer** | Behavioral | WMQMessagingManager | Event-driven messaging |
| 12 | **Repository** | Enterprise | BinMapUploadCRUDOperation, CustomerContractCRUD | Data access abstraction |
| 13 | **DTO/Value Object** | Enterprise | BailmentOnlyPartResponse, BinUploadRow | Data transfer |
| 14 | **Transaction Script** | Enterprise | ConsignmentOrderManager | Transaction boundaries |
| 15 | **Unit of Work** | Enterprise | BinMapUploadCRUDOperation.savebinMapUpload() | Batch transactions |
| 16 | **Filter/Interceptor** | Microservices | BDSICsrfProtectionFilter | Cross-cutting concerns |
| 17 | **REST Resource** | Microservices | CatalogRestResource (JAX-RS) | API exposure |
| 18 | **Caching** | Microservices | AzureKeyVaultService, AzureBlobService | Performance optimization |
| 19 | **Scheduler** | Microservices | BinTransactionProcessor | Scheduled task execution |
| 20 | **Processor/Pipeline** | Microservices | FeedProcessor, IScanTransactionProcessor | Data processing |

---

## 10. Interview Quick Reference

### Q1: What design patterns have you used in your project?

**Answer:**
"In the BDSI Commerce project, I've implemented over 20 design patterns across different categories:

1. **Creational:** Singleton (via ATG Nucleus), Factory (component resolution), Builder (JWT tokens, SQL queries)

2. **Structural:** Adapter (cache to inventory helper), Facade (Azure services), Dependency Injection (all components)

3. **Behavioral:** Strategy (multiple file processors), Template Method (schedulers), Command (form handlers)

4. **Enterprise:** Repository (CRUD operations), DTO (API responses), Transaction Script, Unit of Work

5. **Microservices:** REST Resources (JAX-RS), Filters (CSRF protection), Caching (secrets, clients)"

---

### Q2: Explain the Strategy Pattern in your project?

**Answer:**
"We use Strategy Pattern for scan transaction processing:

```java
// Strategy Interface
public interface IScanTransactionProcessor {
    void process(ConcurrentLinkedQueue<ScanTransactionRequest> scanRequest);
}

// Context resolves strategy dynamically
String processorPath = requestType.getPropertyValue("processor");
Object processor = nucleus.resolveName(processorPath);
((IScanTransactionProcessor) processor).process(requests);
```

Different processors (Receipt Scan, Order Scan, etc.) implement this interface. The scheduler dynamically selects the processor based on request type stored in database. This allows adding new processor types without modifying existing code."

---

### Q3: How do you handle transactions in your project?

**Answer:**
"We use Transaction Script and Unit of Work patterns:

```java
public void saveBulkData(List<DataRow> rows) {
    TransactionDemarcation td = new TransactionDemarcation();
    try {
        td.begin(tm, TransactionDemarcation.REQUIRED);
        
        for (int i = 0; i < rows.size(); i++) {
            // Batch commit every N records (Unit of Work)
            if (i % batchSize == 0 && i > 0) {
                td.end(false);  // Commit batch
                td.begin(tm, TransactionDemarcation.REQUIRED);
            }
            processRow(rows.get(i));
        }
    } catch (Exception e) {
        isRollback = true;
    } finally {
        td.end(isRollback);
    }
}
```

This prevents memory issues with large datasets and provides transactional integrity."

---

### Q4: Explain your caching strategy?

**Answer:**
"We implement caching at multiple levels:

1. **Secret Caching:** Azure Key Vault secrets are cached using ConcurrentHashMap with computeIfAbsent for thread-safe lazy loading.

2. **Client Caching:** Blob storage clients are cached to avoid repeated authentication.

3. **Repository Caching:** ATG Repository provides built-in query result caching.

```java
private final ConcurrentHashMap<String, String> secretCache = new ConcurrentHashMap<>();

public String getSecret(String name) {
    return secretCache.computeIfAbsent(name, this::fetchFromVault);
}
```

---

### Q5: How does Dependency Injection work in ATG?

**Answer:**
"ATG uses Nucleus container for DI, similar to Spring IoC:

1. **Component Definition:** Each class extends GenericService
2. **Configuration:** Properties files define dependencies
3. **Injection:** Nucleus injects dependencies at startup

```java
// Java class
public class MyService extends GenericService {
    @Getter @Setter
    private Repository inventoryRepository;  // Injected
}

// Properties file
$class=com.bdsi.MyService
inventoryRepository=/atg/commerce/inventory/InventoryRepository
```

This achieves loose coupling and testability similar to Spring's @Autowired."

---

## Document Information

| Attribute | Value |
|-----------|-------|
| **Version** | 1.0 |
| **Created** | September 15, 2026 |
| **Author** | Shwetha Kumar |
| **Project** | BDSI Commerce Platform |
| **Technology** | Java, Oracle ATG Commerce, Azure, IBM WMQ |


---


To execute a complex query in Hibernate/JPA without using the @Query annotation, you have three primary type-safe and programmatic alternatives.The best approach is the JPA Criteria API, which is completely type-safe and built into JPA. Alternatively, you can use Querydsl for cleaner syntax, or Specification if you need reusable, dynamic search filters.Here is a breakdown of your options:1. The Standard Way: JPA Criteria APIThe Criteria API allows you to construct queries programmatically using Java objects. It prevents syntax errors at compile-time and is ideal for complex, dynamic queries.java@PersistenceContext
private EntityManager entityManager;

public List<Employee> findComplexEmployees(String department, Double minSalary) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Employee> query = cb.createQuery(Employee.class);
    Root<Employee> employee = query.from(Employee.class);

    // Build complex conditions (AND, OR, Joins)
    Predicate deptPredicate = cb.equal(employee.get("department"), department);
    Predicate salaryPredicate = cb.greaterThan(employee.get("salary"), minSalary);
    
    // Combine predicates and apply to query
    query.select(employee).where(cb.and(deptPredicate, salaryPredicate));

    return entityManager.createQuery(query).getResultList();
}
Use code with caution.2. The Cleanest Way: Spring Data JPA SpecificationIf you are using Spring Data JPA, Specification wraps the Criteria API into a reusable component that you can pass directly into your standard repository interface.Step 1: Extend your Repositoryjavapublic interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    // You automatically get methods like findAll(Specification<T> spec)
}
Use code with caution.Step 2: Define and Execute the Specificationjavapublic class EmployeeSpecifications {
    public static Specification<Employee> hasDepartmentAndMinSalary(String department, Double salary) {
        return (root, query, cb) -> cb.and(
            cb.equal(root.get("department"), department),
            cb.greaterThan(root.get("salary"), salary)
        );
    }
}

// Usage in your service layer:
List<Employee> results = employeeRepository.findAll(
    EmployeeSpecifications.hasDepartmentAndMinSalary("IT", 60000.0)
);
Use code with caution.3. The Fluent Alternative: QuerydslIf the standard Criteria API feels too verbose, Querydsl is a popular framework that generates a "Q-class" for your entities, letting you write fluid, readable queries.java// Requires adding the Querydsl dependency and plugin
QEmployee employee = QEmployee.employee;

List<Employee> results = new JPAQueryFactory(entityManager)
    .selectFrom(employee)
    .where(employee.department.eq("IT")
        .and(employee.salary.gt(60000.0)))
    .fetch();
Use code with caution.Which one should you choose?ApproachProsConsCriteria APIBuilt-in, no extra dependencies, highly dynamic.Verbose and harder to read.SpecificationsSeamlessly integrates with Spring Data Repositories, reusable filters.Still relies on Criteria API syntax under the hood.QuerydslBeautiful, compact, fluent API syntax.Requires extra dependencies and build-step code generation.To help tailor a concrete code example for you, could you share:What entities (tables) and joins are involved in your query?What conditions or dynamic filters do you need to apply?which one is best  ?The JPA Specification approach is the best choice for most applications already using Spring Data JPA.Here is why it usually wins, along with when you should pick the other options instead:Why Specification is Best for Most ProjectsZero Boilerplate: You do not have to write custom repository implementation classes or manage EntityManager transactions manually.Spring Integration: It plugs directly into your existing Spring Data repositories (repository.findAll(spec)).Native Pagination & Sorting: It naturally accepts Spring's Pageable and Sort parameters out of the box, which is usually a nightmare to code manually in the raw Criteria API.When to Choose the AlternativesChoose Querydsl if:Readability is your top priority. Querydsl code reads like clean SQL, whereas the Criteria API/Specifications can get incredibly verbose and ugly for massive queries.You have a massive development team. The generated Q-classes prevent developers from making typos in column names (e.g., writing "deparmtent" instead of "department").Choose Raw Criteria API if:You are not using Spring Data JPA. If you are using a vanilla Jakarta EE / Hibernate setup without Spring, this is your only built-in programmatic option.You are executing highly dynamic projections. If you are constantly transforming query results into various non-entity DTOs on the fly, raw EntityManager calls give you absolute control.Summary RecommendationStart with JPA Specifications. It gives you the best balance of clean architecture, dynamic power, and seamless Spring integration without adding third-party dependencies to your pom.xml or build.gradle.If you would like to see exactly how to implement this for your project, let me know:What database tables or fields are you trying to query?Do you need features like sorting, pagination, or joining multiple tables?


---

*End of Document*
