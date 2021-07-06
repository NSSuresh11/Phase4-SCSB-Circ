package org.recap;

import java.util.Arrays;
import java.util.List;

/**
 * Created by premkb on 19/8/16.
 */
public final class ScsbConstants {

    public static final String GFA = "GFA";
    public static final String RECAP = "RECAP";
    public static  final String FAILURE_UPDATE_CGD = "Failure: User is not allowed to update other instituion Item";
    public static final String INVALID_REQUEST_INSTITUTION = "Please enter valid Institution {0} for requestingInstitution";
    public static final String INVALID_EMAIL_ADDRESS = "Please enter valid emailAddress";
    public static final String START_PAGE_AND_END_PAGE_REQUIRED = "Start page and end page required.";
    public static final String DELIVERY_LOCATION_REQUIRED = "Delivery Location is required for request type Recall/hold/retrieval";
    public static final String INVALID_REQUEST_TYPE = "Please enter the valid request type";
    public static final String EDD_REQUEST = "EDD";
    public static final String REFILED_REQUEST = "REFILED";
    public static final String INVALID_PATRON = "Patron not on file in ILS";
    public static final String ITEMBARCODE_WITH_DIFFERENT_BIB = "All items must be attached to the same bibliographic record, have the same customer code, and the same availability.";
    public static final String INVALID_CUSTOMER_CODE = "Please enter the valid delivery Code";
    public static final String INVALID_DELIVERY_CODE = "Delivery location not valid for this item.";
    public static final String INVALID_TRANSLATED_CODE = "Translated Delivery Location not available for the requested Delivery Location.";
    public static final String RECALL_REQ_INST_ERR_MSG = "Recall request is not supported";
    public static final String RECALL_OWN_INST_ERR_MSG = "Recall request is not supported by item owning institution";

    public static final String INVALID_ITEM_BARCODE = "Item is not available";
    public static final String ITEM_BARCODE_IS_REQUIRED = "Item Barcode is required";
    public static final String MULTIPLE_ITEMS_NOT_ALLOWED_FOR_EDD = "EDD requests must be done on a single item.";
    public static final String WRONG_ITEM_BARCODE = "Item Barcode(s) not available in database.";
    public static final String RETRIEVAL_NOT_FOR_UNAVAILABLE_ITEM = "Item not available for request.";
    public static final String INTERNAL_ERROR_DURING_REQUEST = "Internal error occured while processing the request";
    public static final String RECALL_NOT_FOR_AVAILABLE_ITEM = "Recall not available for this item.";
    public static final String RECALL_FOR_ITEM_EXISTS = "Recall for this item already exists.";
    public static final String INITIAL_LOAD_ITEM_EXISTS = "Initial load item(s) cannot be requested/recalled.";
    public static final String EDD_REQUEST_NOT_ALLOWED = "EDD request is not allowed for this customer code";
    public static final String RECALL_FOR_EDD_ITEM = "Recall for existing EDD request is not allowed";
    public static final String RECALL_FOR_CANCELLED_ITEM = "Recall for Cancelled Request is not Allowed. Please Refile the existing Request.";
    public static final String IMS_LOCATION_DOES_NOT_EXIST_ITEM = "IMS Location does not exist for the item in database.";
    public static final String GENERIC_PATRON_NOT_FOUND_ERROR = "Could not get generic patron for item.";
    public static final String CIRCULATION_FREEZE_UNAVAILABLE_ITEM = "{0} - Item barcode is not available for request. {1}";
    public static final String OWNING_INSTITUTION_CIRCULATION_FREEZE = "The owning institution is on a circulation freeze.";

    public static final String CHAPTER_TITLE_IS_REQUIRED = "Chapter title is required for the request type EDD.";

    // Email
    public static final String REQUEST_RECALL_MAIL_QUEUE = "requestRecallMailSendQueue";
    public static final String REQUEST_LAS_STATUS_MAIL_QUEUE = "requestLASStatusMailSendQueue";
    public static final String REQUEST_ACCESSION_RECONCILATION_MAIL_QUEUE = "AccessionReconcilation";
    public static final String REQUEST_INITIAL_DATA_LOAD = "requestInitialDataLoad";
    public static final String SUBMIT_COLLECTION_EXCEPTION = "Exception";
    public static final String SUBJECT_FOR_SUBMIT_COL_EXCEPTION = "Exception Occured during Submit collection";

    public static final String REQUEST_RECALL_EMAIL_TEMPLATE = "request_recall_email_body.vm";
    public static final String REQUEST_LAS_STATUS_EMAIL_TEMPLATE = "request_las_status_email_body.vm";
    public static final String DELETED_RECORDS_EMAIL_TEMPLATE = "deleted_records_email_body.vm";

    // Retrieval,EDD, Hold, Recall, Borrow Direct
    public static final String REQUEST_TYPE_PW_INDIRECT = "PWI";
    public static final String REQUEST_TYPE_PW_DIRECT = "PWD";

    //Permissions
    public static final String ROLE_SUPER_ADMIN = "Super Admin";
    public static final String ROLE_RECAP = "Repository";
    public static final Boolean BOOLEAN_TRUE = true;
    public static final Boolean BOOLEAN_FALSE = false;

    // MQ URI
    public static final String REQUEST_ITEM_QUEUE = "scsbactivemq:queue:RequestItemQ?asyncConsumer=true&concurrentConsumers=10&destination.consumer.prefetchSize=20";
    public static final String BULK_REQUEST_ITEM_PROCESSING_QUEUE = "scsbactivemq:queue:BulkRequestItemProcessingQ";
    public static final String ASYNC_CONCURRENT_CONSUMERS = "?asyncConsumer=true&concurrentConsumers=";
    public static final String EMAIL_Q = "scsbactivemq:queue:CircEmailQ";
    public static final String SCSB_LAS_OUTGOING_QUEUE_PREFIX = "scsbactivemq:queue:scsb";
    public static final String LAS_OUTGOING_QUEUE_PREFIX = "scsbactivemq:queue:las";
    public static final String SCSB_LAS_OUTGOING_QUEUE_METHOD = "pollLasHeartBeatResponse";
    public static final String LAS_OUTGOING_QUEUE_METHOD = "gfaItemRequestProcessor";
    public static final String BULK_REQUEST_ITEM_QUEUE_METHOD = "bulkRequestItemOnMessage";
    public static final String BULK_REQUEST_ITEM_PROCESSING_QUEUE_METHOD = "bulkRequestProcessItemOnMessage";
    public static final String REQUEST_ITEM_QUEUE_RETRIEVAL_METHOD = "requestItemOnMessage";
    public static final String REQUEST_ITEM_QUEUE_EDD_METHOD = "requestItemEDDOnMessage";
    public static final String REQUEST_ITEM_QUEUE_RECALL_METHOD = "requestItemRecallOnMessage";
    public static final String LAS_INCOMING_QUEUE_RETRIEVAL_METHOD = "lasResponseRetrievalOnMessage";
    public static final String LAS_INCOMING_QUEUE_EDD_METHOD = "lasResponseEDDOnMessage";
    public static final String LAS_INCOMING_QUEUE_PWI_METHOD = "lasResponsePWIOnMessage";
    public static final String LAS_INCOMING_QUEUE_PWD_METHOD = "lasResponsePWDOnMessage";
    public static final String REQUEST_ITEM_LAS_ITEM_STATUS_QUEUE_METHOD = "requestItemLasStatusCheckOnMessage";
    public static final String OUTGOING_QUEUE_SUFFIX = "OutgoingQ";
    public static final String SCSB_OUTGOING_QUEUE = "scsbactivemq:queue:scsbOutgoingQ";
    public static final String LAS_OUTGOING_QUEUE = "scsbactivemq:queue:lasOutgoingQ";
    public static final String LAS_INCOMING_QUEUE = "scsbactivemq:queue:lasIncomingQ";
    public static final String REQUEST_ITEM_LAS_STATUS_CHECK_QUEUE = "scsbactivemq:queue:RequestItemLasStatusCheckQ?asyncConsumer=true";

    public static final String REQUEST_TOPIC_LISTENING_MESSAGES = "Request Topic - Listening to messages";

    //RoutId
    public static final String REQUEST_ITEM_QUEUE_ROUTEID = "RequestItemRouteId";
    public static final String BULK_REQUEST_ITEM_QUEUE_ROUTEID = "BulkRequestItemRouteId";
    public static final String BULK_REQUEST_ITEM_PROCESSING_QUEUE_ROUTEID = "BulkRequestItemProcessingRouteId";
    public static final String EMAIL_ROUTE_ID = "RequestRecallEmailRouteId";
    public static final String SCSB_OUTGOING_ROUTE_ID = "ScsbLasOutgoingRouteId";
    public static final String LAS_OUTGOING_ROUTE_ID = "LasOutgoingQueueRouteId";
    public static final String LAS_INCOMING_ROUTE_ID = "LasIncomingQueueRouteId";
    public static final String REQUEST_ITEM_LAS_STATUS_CHECK_QUEUE_ROUTEID = "RequestItemLasStatusCheckRouteId";
    public static final String REQUEST_TOPIC_ROUTE_ID = "RequestTopicRouteId";
    public static final String EDD_TOPIC_ROUTE_ID = "EDDTopicRouteId";
    public static final String RECALL_TOPIC_ROUTE_ID = "RecallTopicRouteId";
    public static final String REQUEST_TOPIC_ROUTE_METHOD = "requestTopicOnMessage";
    public static final String EDD_TOPIC_ROUTE_METHOD = "eddTopicOnMessage";
    public static final String RECALL_TOPIC_ROUTE_METHOD = "recallTopicOnMessage";

    public static final String REQUEST_ITEM_TOPIC_PREFIX = "RequestItem-";

    public static final String FORMAT_MARC = "marc";
    public static final String FORMAT_SCSB = "scsb";

    public static final String SUBMIT_COLLECTION_COMPLETION_QUEUE_FROM = "scsbactivemq:queue:submitCollectionCompletionFromQueue";
    public static final String SUBMIT_COLLECTION_COMPLETION_QUEUE_TO = "scsbactivemq:queue:submitCollectionCompletionToQueue";
    public static final String PROCESS = "process";
    public static final String SEND_EMAIL_FOR_EMPTY_DIRECTORY = "sendEmailForEmptyDirectory";


    public static final String ITEM_STATUS_AVAILABLE = "Available";
    public static final String INVALID_SCSB_XML_FORMAT_MESSAGE = "Invalid SCSB xml format";
    public static final String INVALID_MARC_XML_FORMAT_MESSAGE = "Invalid Marc xml format";
    public static final String INVALID_MARC_XML_FORMAT_IN_SCSBXML_MESSAGE = "Invalid Marc xml content with in SCSB xml";
    public static final String SUBMIT_COLLECTION_INTERNAL_ERROR = "Internal error occured during submit collection";
    public static final String SUBMIT_COLLECTION_LIMIT_EXCEED_MESSAGE = "Maximum allowed input record is ";
    public static final String BIBRECORD_TAG = "<bibRecords>";
    public static final String SUBMIT_COLLECTION = "submitCollection";
    public static final String SUBMIT_COLLECTION_FOR_NO_FILES = "submitCollectionForNoFiles";

    public static final String BIBLIOGRAPHIC_ENTITY = "bibliographicEntity";

    public static final String GUEST_USER = "Guest";
    public static final String REQUEST_ITEM_HOLD_FAILURE = "RequestItem - Hold Request Failed";
    public static final String REQUEST_ITEM_AVAILABILITY_STATUS_UPDATE = "RequestItem AvailabilityStatus Change";
    public static final String REQUEST_ITEM_GFA_FAILURE = "RequestItem - LAS Request Failed";
    public static final String REQUEST_ITEM_ITEM_CHANGE_LOG_EXCEPTION = "RequestItem - Exception";
    public static final String REQUEST_ITEM_CANCEL_ITEM_AVAILABILITY_STATUS = "RequestItemCancel";
    public static final String REQUEST_ITEM_INSERT = "Request Item Insert";
    public static final String RETRIEVAL_ORDER_NOT_REQUIRED_FOR_RECALL = "Retrieval order not required for recall";


    public static final String REQUEST_ITEM_AVAILABILITY_STATUS_DATA_UPDATE = "1 - 2";
    public static final String REQUEST_ITEM_AVAILABILITY_STATUS_DATA_ROLLBACK = "2 - 1";
    public static final String REQUEST_USE_RESTRICTIONS = "No Restrictions";

    public static final String UPDATE_ITEM_STATUS_SOLR = "/updateItem/updateItemAvailablityStatus";
    public static final String UPDATE_ITEM_STATUS_SOLR_PARAM_ITEM_ID = "itemBarcode";

    public static final String SEARCH_RECORDS_SOLR = "/searchService/searchByParam";
    public static final String SEARCH_RECORDS_SOLR_PARAM_FIELD_VALUE = "fieldValue";
    public static final String SEARCH_RECORDS_SOLR_PARAM_FIELD_NAME = "fieldName";
    public static final String SEARCH_RECORDS_SOLR_PARAM_FIELD_NAME_VALUE = "Barcode";

    public static final int ESIPEXPIRATION_DATE_DAY = 1;
    public static final int ESIPEXPIRATION_DATE_MONTH = 6;

    public static final String REQUEST_STATUS_EXCEPTION = "EXCEPTION";
    public static final String REQUEST_STATUS_PENDING = "PENDING";
    public static final String REQUEST_STATUS_PROCESSING = "PROCESSING";
    public static final String REQUEST_STATUS_LAS_ITEM_STATUS_PENDING = "LAS_ITEM_STATUS_PENDING";
    public static final String LAS_REFILE_REQUEST_PLACED = "LAS_REFILE_REQUEST_PLACED";

    public static final String REST_HOLD_DATE_FORMAT = "yyyy-MM-dd";
    public static final String REST_RECORD_TYPE = "i";

    public static final String REQUEST_PARSE_EXCEPTION = "ParseException : ";
    public static final String REQUEST_INVALID_SIP2_RESPONSE = "InvalidSIP2Response ";
    public static final String REQUEST_INVALID_SIP2_RESPONSE_VALUE = "InvalidSIP2ResponseValue ";
    public static final String REQUEST_ILS_EXCEPTION = "ILS Exception : ";
    public static final String REQUEST_LAS_EXCEPTION = "LAS Exception : ";
    public static final String REQUEST_SCSB_EXCEPTION = "SCSB Exception : ";

    public static final String GFA_SERVICE_PARAM = "filter";

    public static final String GFA_STATUS_NOT_ON_FILE = "NOT ON FILE";
    public static final String GFA_STATUS_SCH_ON_REFILE_WORK_ORDER = "SCH ON REFILE WO:";
    public static final String CANNOT_REFILE_FIRST_SCAN_REQUEST ="Cannot place recall for requests placed on first scan LAS status";
    public static final String REQUESTS_STUCK_IN_PENDING = "Requests stuck in pending";
    public static final String NO_PENDING_REQUESTS_FOUND = "There are no pending requests or no new pending request";
    public static final String RIL_DIRECT_BARCODES_AVAILABLE_IN_LAS = "RILDirectRouteForBarcodesAvailableINLAS";
    public static final String REQUEST_INITIAL_BARCODES_AVAILABLE_IN_LAS = "BarcodesAvailableInLAS";
    public static final String BARCODE_NOT_FOUND_IN_SCSB = "BarcodesNotInSCSB";
    public static final String REQUEST_INITIAL_BARCODES_TO_INDEX = "BarcodesToIndex";

    protected static final List<String> REQUEST_TYPE_LIST = Arrays.asList(ScsbCommonConstants.RETRIEVAL, ScsbCommonConstants.REQUEST_TYPE_EDD, ScsbCommonConstants.BORROW_DIRECT, ScsbCommonConstants.REQUEST_TYPE_RECALL);

    public static final String SUCCESSFULLY_PROCESSED_REQUEST_ITEM = "Successfully Processed Request Item";
    public static final String REQUEST_ITEM_BARCODE_NOT_FOUND = "ITEM BARCODE NOT FOUND.";
    public static final String REQUEST_CANCELLATION_SUCCCESS = "Request cancellation successfully processed";
    public static final String RECALL_CANCELLATION_SUCCCESS = "Recall request cancellation successfully processed";
    public static final String REQUEST_CANCELLATION_EDD_SUCCCESS = "EDD request cancellation successfully processed.";
    public static final String REQUEST_CANCELLATION_NOT_ON_HOLD_IN_ILS = "This Request cannot be canceled, this item is not on hold in ILS";
    public static final String REQUEST_CANCELLATION_NOT_ACTIVE = "RequestId is not active status to be canceled";
    public static final String REQUEST_CANCELLATION_DOES_NOT_EXIST = "RequestId does not exist";

    public static final String GFA_RETRIVAL_ORDER_SUCCESSFUL = "Retrieval order successfully created in LAS.";
    public static final String GFA_RETRIVAL_ITEM_NOT_AVAILABLE = "LAS Exception : Item not available in LAS";
    public static final String GFA_ITEM_STATUS_CHECK_FAILED = "LAS Exception : Item status check failed to return valid response.";
    public static final String SCSB_REQUEST_EXCEPTION = "SCSB Exception : Error occurred at SCSB - ";
    public static final String REQUEST_CANCELLED_NO_REFILED = "canceled";
    public static final String REQUEST_CANCELLED_SUBJECT = "Cancel Request - ";
    public static final String IMS_LOCATION_CODE_BLANK_ERROR = "IMS Location Code is Blank";

    public static final String REQUEST_RECALL_TO_BORRWER = "recalled";
    public static final String REQUEST_RECALL_SUBJECT = "Recall - ";

    public static final String REQUEST_REFILE_BODY = "The following item has been requested and was found to be under Refile in LAS.";
    public static final String REQUEST_REFILE_SUBJECT = "Request for Item on Refile WO - ";

    //Deaccession
    public static final String DEACCESSION_IN_SOLR_URL = "deaccessionInSolrService/deaccessionInSolr";
    public static final String DEACCESSION_NO_BARCODE_ERROR = "Provide one or more barcodes to deaccession";
    public static final String DEACCESSION_NO_BARCODE_PROVIDED_ERROR = "No barcode provided for deaccession";
    public static final String REQUEST_ITEM_CANCEL_DEACCESSION_ITEM = "RequestItemCancel DeaccessionItem";
    public static final String REQUEST_ITEM_CANCELED_FOR_DEACCESSION = "The request for this item has been canceled because the item has been deaccessioned.";
    public static final String REASON_CANCEL_REQUEST_FAILED = "Canceling hold for the requested item failed for the reason";
    public static final String DISCOVERY = "Discovery";

    public static final String DATE_FORMAT_FOR_FILE_NAME = "yyyyMMdd_HHmmss";

    //Logger
    public static final String GFA_ITEM_STATUS_MISMATCH = "There is a mismatch in item status between SCSB and LAS. Please contact ReCAP staff (<a href=\"mailto:{0}\">{1}</a>) for further assistance.";
    public static final String GFA_SERVER_DOWN = "LAS item status check failed to give valid response or LAS is down. Please contact ReCAP staff (<a href=\"mailto:{0}\">{1}</a>) for further assistance.";
    public static final String LAS_DEACCESSION_REJECT_ERROR = "LAS has rejected the {0} request with Error code : \"{1}\" and Error note : \"{2}\".";
    public static final String LAS_SERVER_NOT_REACHABLE_ERROR = "LAS server is not reachable. Please contact ReCAP staff (<a href=\"mailto:{0}\">{1}</a>) for further assistance.";
    public static final String CREATING_A_BIB_RECORD_FAILED_IN_ILS = "Creating a Bib record failed in ILS";
    public static final String INVALID_NO_RESPONSE_FROM_ILS = "Invalid/No Response from ILS";
    public static final String CHECK_IN_REQUEST_FAILED = "Check In Request Failed";
    public static final String ILS_LOGIN_FAILED = "Login Failed";
    public static final String ITEM_STATUS_REQUEST_FAILED = "Item Status Request Failed";
    public static final String RECALL_FAILED_NO_MESSAGE_RETURNED = "Recall failed, no message returned";
    public static final String RECALL_CANNOT_BE_PROCESSED_THE_ITEM_IS_NOT_CHECKED_OUT_IN_ILS = "Recall Cannot be processed, the item is not checked out in ILS";
    public static final String FINISH_PROCESSING = "Finish Processing";

    public static final String EMAIL_BODY_FOR = "emailBodyFor";
    public static final String SUBMIT_COLLECTION_SUCCESS_LIST = "submitCollectionSuccessList";
    public static final String SUBMIT_COLLECTION_FAILURE_LIST = "submitCollectionFailureList";
    public static final String SUBMIT_COLLECTION_REJECTION_LIST = "submitCollectionRejectionList";
    public static final String SUBMIT_COLLECTION_EXCEPTION_LIST = "submitCollectionExceptionList";
    public static final String SUBMIT_COLLECTION_EXCEPTION_RECORD = "Exception record - Item is unavailable in scsb to update";
    public static final String SUBMIT_COLLECTION_DEACCESSION_EXCEPTION_RECORD = "Exception record - Item not updated, it is a deaccessioned item";
    public static final String SUBMIT_COLLECTION_REJECTION_RECORD = "Rejection record - Only use restriction and cgd not updated because the item is in use";
    public static final String SUBMIT_COLLECTION_SUCCESS_RECORD = "Success record";
    public static final String SUBMIT_COLLECTION_FAILED_RECORD = "Failed record";
    public static final String REST = "rest-api";
    public static final String SUBMIT_COLLECTION_EMAIL_BODY_VM = "submit_collection_email_body.vm";
    public static final String SUBMIT_COLLECTION_EMAIL_BODY_FOR_EMPTY_DIRECTORY_VM = "submit_collection_email_body_for_emptyDirectory.vm";
    public static final String PROCESS_INPUT = "processInput";
    public static final String SUBMIT_COLLECTION_SFTP_OPTIONS = "&sendEmptyMessageWhenIdle=true&move=.done&sortBy=file:modified&localWorkDirectory=";
    public static final String SUBMIT_COLLECTION_COMPLETE_RECORD_UPDATE = "Complete item record info updated through submit collection";
    public static final String SUBMIT_COLLECTION_DUMMY_RECORD_UPDATE = "Dummy item record removed and actual record added through submit collection";

    public static final String DEACCESSION_ROLLBACK = "Deaccession Rollback";
    public static final String DEACCESSION_ROLLBACK_NOTES = " Hence, the transaction of deaccessioning item is rolled back.";

    public static final String COUNT_OF_PURGED_EXCEPTION_REQUESTS = "countOfPurgedExceptionRequests";

    public static final String REQUEST_DATA_LOAD_CREATED_BY = "LAS";
    public static final String REQUEST_DATA_LOAD_REQUEST_TYPE = "PHY";
    public static final String REQUEST_DATA_LOAD_PATRON_ID = "0000000";
    public static final String REQUEST_DATA_LOAD_ITEM_ID = "itemId";
    public static final String REQUEST_DATA_LOAD_REQUESTING_INST_ID = "requestingInstitutionId";
    public static final String USE_RESTRICTION_UNAVAILABLE = "use restriction is unavailable in the input xml";
    public static final String CGD_NA = "cgd is still in NA status, provide cdg in input xml";
    public static final String RECORD_INCOMPLETE = "Record continue to be incomplete because ";
    public static final String DELETED_RECORDS_SUCCESS_MSG = "Deleted records completed successfully";
    public static final String DELETED_RECORDS_FAILURE_MSG = "Deleted records failed due to unexpected error";

    public static final String DELETED_STATUS_NOT_REPORTED = "Not Reported";
    public static final String DELETED_STATUS_REPORTED = "Reported";
    public static final String DELETED_MAIL_TO = "DELETED_MAIl_TO";
    public static final String EMAIL_SUBJECT_DELETED_RECORDS = "List of Deleted Records";
    public static final String EMAIL_DELETED_RECORDS_DISPLAY_MESSAGE = "Total No. of Records Deleted : ";
    public static final String DAILY_RECONCILIATION = "DailyReconciliation";

    //Daily Reconcilation
    public static final String DAILY_RR_FTP_ROUTE_ID = "DailyReconcilationFtpRoute";
    public static final String DAILY_RR_FS_ROUTE_ID = "DailyReconcilationFsRoute";
    public static final String DAILY_RR_FTP_OPTIONS = "&move=.done&delay=2&localWorkDirectory=";
    public static final String DAILY_RR_FS_OPTIONS = "?delete=true";
    public static final String DAILY_RR_FS_FILE = "file:";
    public static final String DAILY_RR_LAS = "LAS";
    public static final String DAILY_RR_SCSB = "SCSB";
    public static final String DAILY_RR_COMPARISON = "Comparison";
    public static final String DAILY_RR_FILE_DATE_FORMAT = "yyyyMMdd_HHmmss";
    public static final String DAILY_RR = "DailyReconciliation_";
    public static final String DATE_CELL_STYLE_FORMAT = "MM/dd/yyyy HH:mm:ss.S";
    public static final String DAILY_RR_REQUEST_ID = "RequestId";
    public static final String DAILY_RR_BARCODE = "Barcode";
    public static final String DAILY_RR_CUSTOMER_CODE = "CustomerCode";
    public static final String DAILY_RR_STOP_CODE = "StopCode";
    public static final String DAILY_RR_PATRON_ID = "PatronId";
    public static final String DAILY_RR_CREATED_DATE = "CreatedDate";
    public static final String DAILY_RR_LAST_UPDATED_DATE = "LastUpdatedDate";
    public static final String DAILY_RR_REQUESTING_INST = "RequestingInstitution";
    public static final String DAILY_RR_OWNING_INSTITUTION = "OwningInstitution";
    public static final String DAILY_RR_DELIVERY_METHOD = "DeliveryMethod";
    public static final String DAILY_RR_STATUS = "Status";
    public static final String DAILY_RR_EMAIL = "Email";
    public static final String DAILY_RR_MATCHED = "Matched";
    public static final String DAILY_RR_MISMATCH = "Mismatch";
    public static final String DAILY_RR_LAS_NOT_GIVEN_STATUS = "LASNotGivenStatus";
    public static final String DAILY_RR_SCSB_NOT_GIVEN_STATUS = "StatusNotFoundInScsb";
    public static final String DAILY_RR_NOT_IN_SCSB = "NotInScsb";

    //status Reconciliation

    public static final String STATUS_RECONCILIATION_REPORT = "scsbactivemq:queue:statusReconciliationReportQ";
    public static final String STATUS_RECONCILIATION_REPORT_ID = "statusReconciliationReportRoute";

    public static final String COMPLETE = "Complete";

    public static final String ACCESSION_RR_FTP_OPTIONS = "&sendEmptyMessageWhenIdle=true&move=.done&delay=2&localWorkDirectory=";
    public static final String ACCESSION_RECONCILATION_FILE_NAME = "AccessionReconcilation";
    public static final String ACCESSION_RECONCILATION_SOLR_CLIENT_URL = "accessionReconcilationService/startAccessionReconcilation";
    public static final String STARTING = "Starting ";
    public static final String SUBMIT_COLLECTION_COMPLETED_ROUTE = "submitCollectionCompletedRoute";
    public static final String SUBMIT_COLLECTION_CAUGHT_EXCEPTION_METHOD = "caughtException";
    public static final String SUBMIT_COLLECTION_EXCEPTION_BODY_VM = "submit_collection_exception_body.vm";
    public static final String DELETED_MAIL_QUEUE = "deletedRecordsMailSendQueue";
    public static final String COUNT_OF_PURGED_ACCESSION_REQUESTS = "countOfPurgedAccessionRequests";
    public static final String STATUS_RECONCILIATION_CHANGE_LOG_OPERATION_TYPE = "StatusReconciliation-ItemAvailablityStatusChange";
    public static final String REQUEST_DATA_LOAD_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
    public static final String REQUEST_INITIAL_LOAD_FS_FILE = "file:";
    public static final String REQUEST_INITIAL_LOAD_FS_ROUTE = "requestInitialLoadFsRoute";
    public static final String REQUEST_INITIAL_LOAD_FTP_ROUTE = "requestInitialLoadFTPRoute";
    public static final String REQUEST_INITIAL_FILE_NAME = "InitialRequestLoadBarcodeFail_";
    public static final String FOR = "for";
    public static final String STATUS_RECONCILIATION = "StatusReconciliation";
    public static final String STATUS_RECONCILIATION_FAILURE = "StatusReconciliationFailure";
    public static final String CAMEL_SPLIT_INDEX = "CamelSplitIndex";
    public static final String REQUEST_INITIAL_LOAD_HEADER = "Barcodes Not Exist In SCSB";
    public static final String EMAIL_HEADER_REQUEST_PENDING = "Request_Pending";
    public static final String REQUEST_PENDING_EMAIL_BODY_VM = "requestPendingEmailBody.vm";

    // REST API URL
    public static final String REST_CHECKOUT_REQUEST_URL = "/checkout-requests";
    public static final String REST_CHECKIN_REQUEST_URL = "/checkin-requests";
    public static final String REST_RECAP_HOLD_REQUEST_URL = "/recap/hold-requests";
    public static final String REST_RECAP_CANCEL_HOLD_REQUEST_URL = "/recap/cancel-hold-requests";
    public static final String REST_RECAP_RECALL_REQUEST_URL = "/recap/recall-requests";
    public static final String REST_RECAP_REFILE_REQUEST_URL = "/recap/refile-requests";
    public static final String REST_HOLD_REQUEST_URL = "/hold-requests";
    public static final String REST_PATRON_BY_BARCODE_URL = "/patrons?barcode=";

    //Date Pattern
    public static final String FILE_DATE_FORMAT = "yyyyMMdd_HHmmss";
    public static final String CHECK_IN_REQUEST_SUCCESSFUL = "Check In Request Successful";
    public static final String SCREEN_MESSAGE_ITEM_BARCODE_NOT_FOUND = "Item barcode not found";
    public static final String ITEM_BARCODE_NOT_FOUND = "ITEM_BARCODE_NOT_FOUND";
    public static final String ITEM_REQUEST_FAILED = "Item Request Failed";
    public static final String PATRON_VALIDATION_FAILED = "Patron Validation Failed: ";
    public static final String ILS_CONNECTION_FAILED = "ILS Connection Failed";

    public static final String CAMEL_SPLIT_COMPLETE = "CamelSplitComplete";
    public static final String DIRECT = "direct:";
    public static final String PROCESS_DAILY_RECONCILIATION = "processDailyReconciliaion";
       public static final String REQUEST_INITIAL_LOAD_DIRECT_ROUTE = "requestInitialLoadDirectRoute";
    public static final String ACCESSION_RECONCILIATION_HEADER = "Barcodes not present in SCSB";

    public static final String LAS = "LAS";
    public static final String USER = "User";
    public static final String REQUEST_RECALL = "RequestItem-Recall";
    public static final String REQUEST_RETRIEVAL = "RequestItem-Retrieval";
    public static final String PROCESSED = "PROCESSED";
    public static final String BULK_REQUEST_EMAIL_QUEUE = "BulkRequestEmailQueue";
    public static final String BULK_REQUEST_EMAIL_BODY_VM = "bulkRequestEmailBody.vm";

    public static final String REST_NO_RESTRICTIONS = "Standard {0} restrictions apply";

    public static final String BARCODE_RECONCILIATION_FILE_DATE_FORMAT = "yyyyMMdd";

    public static final String SUBMIT_COLLECTION_JOB_INITIATE_ROUTE_ID = "scsbactivemq:queue:submitCollectionInitiateRoute";
    public static final String BARCODE_NOT_FOUND_IN_LAS = "Barcode not found in LAS";
    public static final String CUSTOMER_CODE_HEADER = "Customer Code mentioned in LAS";
    public static final String TAB = "\t";
    public static final String NEW_LINE = "\n";

    public static final String DEFAULT_DATE_FORMAT = "dd-MM-yyyy hh:mm";
    public static final String REQUEST_IDS_RANGE = "RangeOfRequestIds";
    public static final String REQUEST_DATES_RANGE = "RangeOfRequestDates";
    public static final String NO_REQUESTS_FOUND = "No requests found.";
    public static final String IGNORE_REQUEST_TYPE_NOT_VALID = "Ignored as the request type is ";
    public static final String REQUEST_STATUS_INVALID = "Provide request status value. Allowed values are : PENDING, EXCEPTION";
    public static final String REQUEST_IDS_INVALID = "Provide comma separated request Ids or Provide request status value. Allowed values are : PENDING, EXCEPTION.";
    public static final String REQUEST_START_END_IDS_INVALID = "Provide start request id and end request id or Provide request status value. Allowed values are : PENDING, EXCEPTION.";
    public static final String REQUEST_DATES_INVALID = "Provide request from date and to date or Provide request status value. Allowed values are : PENDING, EXCEPTION.";
    public static final String REQUEST_REPLACE_BY_TYPE_INVALID = "Invalid replace request type. Allowed values for \"replaceRequestByType\" are :  RequestStatus, RequestIds, RangeOfRequestIds, RangeOfRequestDates";
    public static final String REQUEST_REPLACE_BY_TYPE_NOT_SELECTED = "Provide one of the values to \"replaceRequestByType\" :  RequestStatus, RequestIds, RangeOfRequestIds, RangeOfRequestDates";

    public static final String EMAIL_HEADER_REQUEST_STATUS_PENDING = "Requests in pending status";
    public static final String EMAIL_SUBJECT_FOR_PENDING_STATUS = "Requests stuck in PENDING status";
    public static final String EMAIL_SUBJECT_FOR_LAS_PENDING_STATUS = "Requests in LAS ITEM STATUS PENDING status";
    public static final String EMAIL_SUBJECT_FOR_PENDING_AND_LAS_STATUS = "Requests in PENDING and LAS ITEM STATUS PENDING status";

    /**
     * 0 = Initialise Polling Processess
     * 1 = Started Polling Processess
     * 2 = Finish Polling Processess
     *
     */
    public static int LAS_ITEM_STATUS_REST_SERVICE_STATUS = 0;

    public static final String BULK_REQUEST_ID_TEXT = "Bulk Request Id : ";

    public static final String ITEM_STATUS_NOT_AVAILABLE = "Not Available";

    private ScsbConstants() {
    }

    public static List<String> getRequestTypeList() {
        return REQUEST_TYPE_LIST;
    }

    public static final String ETL_DATA_LOAD_NAMESPACE = "http://www.loc.gov/MARC21/slim";

    /* Institution and Protocol Specific Properties */
    public static final String SIP2_PROTOCOL = "SIP2";
    public static final String NCIP_PROTOCOL = "NCIP";
    public static final String REST_PROTOCOL = "REST";

    public static final String PROTOCOL = "protocol";
    public static final String SCSB_CAMEL_S3_TO_ENDPOINT = "aws-s3://{{scsbBucketName}}?autocloseBody=false&region={{awsRegion}}&accessKey=RAW({{awsAccessKey}})&secretKey=RAW({{awsAccessSecretKey}})";
    public static final String DATE_FORMAT = "MM-dd-yyyy HH:mm:ss";

    public static final String TOOLKIT_PROP_FILE = "toolkit.properties";
    public static final String BIBLIOGRAPHIC_DESCRIPTION = "bibliographicDescription";
    public static final String AGENCY_ID_SCSB = "SCSB";

    public static final String AUTHOR = "Author";
    public static final String CALL_NUMBER = "call number";
    public static final String CIRCULATION_STATUS = "Circulation Status";
    public static final String DUE_DATE = "dueDate";
    public static final String ISBN = "ISBN";
    public static final String ISSN = "ISSN";
    public static final String ITEM_ID = "itemId";
    public static final String ITEM_DESCRIPTION = "itemDescription";
    public static final String ITEM_USE_RESTRICTION = "Item Use Restriction Type";
    public static final String LOANED_ITEMS = "Loaned Items";
    public static final String LOCATION = "Location";
    public static final String NAME_INFORMATION = "Name Information";
    public static final String PUBLISHER = "Publisher";
    public static final String PUBLICATION_DATE = "Publication Date";
    public static final String REQUESTED_ITEMS = "Requested Items";
    public static final String REQUEST_ID = "requestId";
    public static final String TITLE = "Title";
    public static final String USER_ADDRESS_INFORMATION = "User Address Information";
    public static final String USER_FISCAL_INFORMATION = "User Fiscal Information";
    public static final String USER_LANGUAGE_INFORMATION= "User Language Information";
    public static final String USER_PRIVILEGE = "User Privilege";
    public static final String USERID = "User Id";
    public static final String HOLD = "Hold";
    public static final String ITEM_BARCODE = "itemBarcode";
    public static final String PATRON_IDENTIFIER = "patronIdentifier";

    public static final String RECAP_DEPOSITORY = "RD";
    public static final String NCIP_REMOTE_STORAGE = "NCIP_REMOTE_STORAGE";
    public static final String ILS_SOURCE_FOR_ITEM = "ils.source.for.item.";

}
