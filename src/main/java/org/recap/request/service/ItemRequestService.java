package org.recap.request.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.engine.DefaultFluentProducerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbConstants;
import org.recap.ScsbCommonConstants;
import org.recap.model.request.ItemRequestInformation;
import org.recap.ims.service.GFALasService;
import org.recap.model.ItemRefileRequest;
import org.recap.controller.RequestItemController;
import org.recap.model.response.ItemCreateBibResponse;
import org.recap.model.response.ItemHoldResponse;
import org.recap.model.request.ReplaceRequest;
import org.recap.model.response.ItemInformationResponse;
import org.recap.model.response.ItemRecallResponse;
import org.recap.model.jpa.*;
import org.recap.model.response.ItemRefileResponse;
import org.recap.model.search.SearchResultRow;
import org.recap.repository.jpa.*;
import org.recap.service.RestHeaderService;
import org.recap.util.CommonUtil;
import org.recap.request.util.ItemRequestServiceUtil;
import org.recap.util.PropertyUtil;
import org.recap.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class for Request Item Service
 * Created by sudhishk on 1/12/16.
 */
@Component
public class ItemRequestService {


    private static final Logger logger = LoggerFactory.getLogger(ItemRequestService.class);

    @Value("${" + PropertyKeyConstants.SCSB_SOLR_DOC_URL + "}")
    private String scsbSolrClientUrl;

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private RequestItemController requestItemController;

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    private DeliveryCodeDetailsRepository deliveryCodeDetailsRepository;


    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    @Autowired
    private GFALasService gfaLasService;

    @Autowired
    private ItemRequestDBService itemRequestDBService;

    @Autowired
    private OwnerCodeDetailsRepository ownerCodeDetailsRepository;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;


    @Autowired
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Autowired
    private RestHeaderService restHeaderService;

    @Autowired
    private ItemRequestServiceUtil itemRequestServiceUtil;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private RequestParamaterValidatorService requestParamaterValidatorService;

    @Autowired
    private DeliveryCodeTranslationDetailsRepository deliveryCodeTranslationDetailsRepository;


    @Autowired
    private ItemValidatorService itemValidatorService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ItemEDDRequestService itemEDDRequestService;

    @Autowired
    private GenericPatronDetailsRepository genericPatronDetailsRepository;

    @Autowired
    private PropertyUtil propertyUtil;

    /**
     * @return
     */
    public RestHeaderService getRestHeaderService() {
        return restHeaderService;
    }

    /**
     * @return
     */
    public EmailService getEmailService() {
        return emailService;
    }

    /**
     * @return
     */
    public GFALasService getGfaLasService() {
        return gfaLasService;
    }

    /**
     * Request item item information response.
     *
     * @param itemRequestInfo the item request info
     * @param exchange        the exchange
     * @return the item information response
     */
    public ItemInformationResponse requestItem(ItemRequestInformation itemRequestInfo, Exchange exchange) {

        List<ItemEntity> itemEntities;
        ItemEntity itemEntity;
        ItemInformationResponse itemResponseInformation = new ItemInformationResponse();
        try {
            itemEntities = itemDetailsRepository.findByBarcodeIn(itemRequestInfo.getItemBarcodes());

            if (itemEntities != null && !itemEntities.isEmpty()) {
                itemEntity = itemEntities.get(0);
                if (StringUtils.isBlank(itemRequestInfo.getBibId())) {
                    itemRequestInfo.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                }
                itemRequestInfo.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
                itemRequestInfo.setImsLocationCode(itemEntity.getImsLocationEntity().getImsLocationCode());

                InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionCode(itemRequestInfo.getRequestingInstitution());
                DeliveryCodeEntity deliveryCodeEntity = deliveryCodeDetailsRepository.findByDeliveryCodeAndOwningInstitutionIdAndActive(itemRequestInfo.getDeliveryLocation(), institutionEntity.getId(), 'Y');

                DeliveryCodeTranslationEntity deliveryCodeTranslationEntity = deliveryCodeTranslationDetailsRepository.findByRequestingInstitutionandImsLocation(institutionEntity.getId(), deliveryCodeEntity.getId(), itemEntity.getImsLocationEntity().getId());
                if (deliveryCodeTranslationEntity != null && deliveryCodeTranslationEntity.getImsLocationDeliveryCode() != null) {
                    logger.info("Translation Code >>>> {} "  , deliveryCodeTranslationEntity.getImsLocationDeliveryCode());
                    itemRequestInfo.setTranslatedDeliveryLocation(deliveryCodeTranslationEntity.getImsLocationDeliveryCode());
                } else {
                    itemResponseInformation.setScreenMessage(ScsbConstants.REQUEST_SCSB_EXCEPTION + ScsbConstants.INVALID_TRANSLATED_CODE);
                    itemResponseInformation.setSuccess(false);
                }
                logger.info("itemEntity.getImsLocationEntity().getImsLocationCode() >>>> {} "  , itemEntity.getImsLocationEntity().getImsLocationCode());
                SearchResultRow searchResultRow = searchRecords(itemEntity); //Solr

                itemRequestInfo.setTitleIdentifier(getTitle(itemRequestInfo.getTitleIdentifier(), itemEntity, searchResultRow));
                itemRequestInfo.setAuthor(searchResultRow.getAuthor());
                itemRequestInfo.setCustomerCode(itemEntity.getCustomerCode());
                if (deliveryCodeEntity != null) {
                    itemRequestInfo.setPickupLocation(deliveryCodeEntity.getPickupLocation());
                }
                itemResponseInformation.setItemId(itemEntity.getId());

                boolean isItemStatusAvailable;
                synchronized (this) {
                    // Change Item Availablity
                    isItemStatusAvailable = updateItemAvailabilityStatus(itemEntities, itemRequestInfo.getUsername());
                }

                Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, ScsbConstants.REQUEST_STATUS_PROCESSING);
                itemRequestInfo.setRequestId(requestId);
                itemResponseInformation.setRequestId(requestId);

                if (requestId == 0) {
                    rollbackUpdateItemAvailabilityStatus(itemEntity, itemRequestInfo.getUsername());
                    itemResponseInformation.setScreenMessage(ScsbCommonConstants.REQUEST_EXCEPTION + ScsbConstants.INTERNAL_ERROR_DURING_REQUEST);
                    itemResponseInformation.setSuccess(false);
                } else if (!isItemStatusAvailable) {
                    itemResponseInformation.setScreenMessage(ScsbConstants.REQUEST_SCSB_EXCEPTION + ScsbConstants.RETRIEVAL_NOT_FOR_UNAVAILABLE_ITEM);
                    itemResponseInformation.setSuccess(false);
                } else {
                    // Process
                    itemResponseInformation = checkOwningInstitution(itemRequestInfo, itemResponseInformation, itemEntity);
                }
            } else {
                itemResponseInformation.setScreenMessage(ScsbConstants.REQUEST_SCSB_EXCEPTION + ScsbConstants.WRONG_ITEM_BARCODE);
                itemResponseInformation.setSuccess(false);
            }
            setItemResponseInformation(itemRequestInfo, itemResponseInformation);
            logger.info("itemRequestInfo.getImsLocationCode() before LAS Call >>>> {} "  , itemRequestInfo.getImsLocationCode());
            if (isUseQueueLasCall(itemRequestInfo.getImsLocationCode()) && (StringUtils.containsIgnoreCase(itemResponseInformation.getScreenMessage(), ScsbConstants.REQUEST_ILS_EXCEPTION)
                    || StringUtils.containsIgnoreCase(itemResponseInformation.getScreenMessage(), ScsbConstants.REQUEST_SCSB_EXCEPTION)
                    || StringUtils.containsIgnoreCase(itemResponseInformation.getScreenMessage(), ScsbConstants.REQUEST_LAS_EXCEPTION))) {
                updateChangesToDb(itemResponseInformation, ScsbConstants.REQUEST_RETRIEVAL + "-" + itemResponseInformation.getRequestingInstitution());
            }
            // Update Topics
            sendMessageToTopic(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getRequestType(), itemResponseInformation, exchange);
            logger.info(ScsbConstants.FINISH_PROCESSING);
        } catch (RestClientException ex) {
            logger.error(ScsbCommonConstants.REQUEST_EXCEPTION_REST, ex);
        } catch (Exception ex) {
            logger.error(ScsbCommonConstants.REQUEST_EXCEPTION, ex);
        }
        return itemResponseInformation;
    }

    /**
     * Update request status based on success message from ILS
     *
     * @param itemResponseInformation
     * @param operationType
     */
    public void updateChangesToDb(ItemInformationResponse itemResponseInformation, String operationType) {
        if (itemResponseInformation.getRequestId() != null && itemResponseInformation.getRequestId() > 0) {
            saveItemChangeLogEntity(itemResponseInformation.getRequestId(), getUser(itemResponseInformation.getUsername()), operationType, itemResponseInformation.getRequestNotes());
            updateRecapRequestItem(itemResponseInformation);
        }
    }

    /**
     * Recall item item information response.
     *
     * @param itemRequestInfo the item request info
     * @param exchange        the exchange
     * @return the item information response
     */
    public ItemInformationResponse recallItem(ItemRequestInformation itemRequestInfo, Exchange exchange) {

        List<ItemEntity> itemEntities;
        ItemEntity itemEntity;
        ItemInformationResponse itemResponseInformation = new ItemInformationResponse();
        try {
            itemEntities = itemDetailsRepository.findByBarcodeIn(itemRequestInfo.getItemBarcodes());
            RequestItemEntity requestItemEntity = requestItemDetailsRepository.findByItemBarcodeAndRequestStaCode(itemRequestInfo.getItemBarcodes().get(0), ScsbCommonConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
            if (requestItemEntity != null) {
                if (itemEntities != null && !itemEntities.isEmpty()) {
                    itemEntity = itemEntities.get(0);
                    SearchResultRow searchResultRow = searchRecords(itemEntity); //Solr

                    itemRequestInfo.setTitleIdentifier(getTitle(itemRequestInfo.getTitleIdentifier(), itemEntity, searchResultRow));
                    itemRequestInfo.setAuthor(searchResultRow.getAuthor());
                    itemRequestInfo.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                    itemRequestInfo.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
                    itemRequestInfo.setImsLocationCode(itemEntity.getImsLocationEntity().getImsLocationCode());
                    InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionCode(itemRequestInfo.getRequestingInstitution());
                    itemRequestInfo.setPickupLocation(getPickupLocation(institutionEntity.getId(), itemRequestInfo.getDeliveryLocation()));
                    itemResponseInformation.setItemId(itemEntity.getId());
                    Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, ScsbConstants.REQUEST_STATUS_PROCESSING);
                    itemRequestInfo.setRequestId(requestId);
                    itemResponseInformation.setRequestId(requestId);

                    if (requestId == 0) {
                        itemResponseInformation.setScreenMessage(ScsbCommonConstants.REQUEST_EXCEPTION + ScsbConstants.INTERNAL_ERROR_DURING_REQUEST);
                        itemResponseInformation.setSuccess(false);
                    } else {
                        checkOwningInstitutionRecall(itemRequestInfo, itemResponseInformation, itemEntity);
                    }
                } else {
                    itemResponseInformation.setScreenMessage(ScsbConstants.REQUEST_SCSB_EXCEPTION + ScsbConstants.WRONG_ITEM_BARCODE);
                    itemResponseInformation.setSuccess(false);
                }
            } else {
                itemResponseInformation.setScreenMessage(ScsbConstants.REQUEST_SCSB_EXCEPTION + ScsbConstants.CANNOT_REFILE_FIRST_SCAN_REQUEST);
                itemResponseInformation.setSuccess(false);
            }
            logger.info(ScsbConstants.FINISH_PROCESSING);
            setItemResponseInformation(itemRequestInfo, itemResponseInformation);

            if (isUseQueueLasCall(itemRequestInfo.getImsLocationCode())) {
                updateChangesToDb(itemResponseInformation, ScsbConstants.REQUEST_RECALL + "-" + itemResponseInformation.getRequestingInstitution());
            }
            // Update Topics
            sendMessageToTopic(itemRequestInfo.getItemOwningInstitution(), itemRequestInfo.getRequestType(), itemResponseInformation, exchange);
        } catch (RestClientException ex) {
            logger.error(ScsbCommonConstants.REQUEST_EXCEPTION_REST, ex);
            itemResponseInformation.setScreenMessage(ScsbConstants.REQUEST_SCSB_EXCEPTION + ScsbConstants.INTERNAL_ERROR_DURING_REQUEST);
            itemResponseInformation.setSuccess(false);
        } catch (Exception ex) {
            logger.error(ScsbCommonConstants.REQUEST_EXCEPTION, ex);
            itemResponseInformation.setScreenMessage(ScsbConstants.REQUEST_SCSB_EXCEPTION + ScsbConstants.INTERNAL_ERROR_DURING_REQUEST);
            itemResponseInformation.setSuccess(false);
        }
        return itemResponseInformation;
    }

    /**
     * Re file item boolean.
     *
     * @param itemRefileRequest the item refile request
     * @return the boolean
     */
    public ItemRefileResponse reFileItem(ItemRefileRequest itemRefileRequest, ItemRefileResponse itemRefileResponse) {

        logger.info("Processing received Refile request");
        logger.info("Refile Request Information : Barcodes {} , Request Id's : {}", itemRefileRequest.getItemBarcodes(), itemRefileRequest.getRequestIds());
        // Change Response for this Method
        String itemBarcode;
        ItemEntity itemEntity;
        List<String> requestItemStatusList = Arrays.asList(ScsbCommonConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED, ScsbCommonConstants.REQUEST_STATUS_EDD, ScsbCommonConstants.REQUEST_STATUS_CANCELED, ScsbCommonConstants.REQUEST_STATUS_INITIAL_LOAD);
        List<RequestItemEntity> requestEntities = requestItemDetailsRepository.findByIdsAndStatusCodes(itemRefileRequest.getRequestIds(), requestItemStatusList);
        List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.findByItemBarcodes(itemRefileRequest.getItemBarcodes());
        if (requestEntities != null && !requestEntities.isEmpty()) {
            for (RequestItemEntity requestItemEntity : requestEntities) {
                itemEntity = requestItemEntity.getItemEntity();
                RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ScsbCommonConstants.REQUEST_STATUS_REFILED);
                String gfaItemStatus = gfaLasService.callGfaItemStatus(itemEntity.getBarcode());
                logger.info("GFA Item Status {} for the barcode {} received on Refile", gfaItemStatus, itemEntity.getBarcode());
                if (itemEntity.getItemAvailabilityStatusId() == 2) { // Only Item Not Availability, Status is Processed
                    itemBarcode = itemEntity.getBarcode();
                    ItemRequestInformation itemRequestInfo = new ItemRequestInformation();
                    itemRequestInfo.setItemBarcodes(Collections.singletonList(itemBarcode));
                    itemRequestInfo.setItemOwningInstitution(requestItemEntity.getItemEntity().getInstitutionEntity().getInstitutionCode());
                    itemRequestInfo.setRequestingInstitution(requestItemEntity.getInstitutionEntity().getInstitutionCode());
                    itemRequestInfo.setRequestType(requestItemEntity.getRequestTypeEntity().getRequestTypeCode());
                    RequestItemEntity requestItemEntityRecalled = requestItemDetailsRepository.findByItemBarcodeAndRequestStaCode(itemBarcode, ScsbCommonConstants.REQUEST_STATUS_RECALLED);
                    if (requestItemEntityRecalled == null) { // Recall Request Does not Exist
                        requestItemEntity.setRequestStatusId(requestStatusEntity.getId());
                        requestItemEntity.setLastUpdatedDate(new Date());
                        requestItemDetailsRepository.save(requestItemEntity);
                        rollbackUpdateItemAvailabilityStatus(itemEntity, ScsbConstants.GUEST_USER);
                        itemRequestServiceUtil.updateSolrIndex(itemEntity);
                        itemRefileResponse.setSuccess(true);
                    } else { // Recall Request Exist
                        if (requestItemEntityRecalled.getRequestingInstitutionId().intValue() == requestItemEntity.getRequestingInstitutionId().intValue()) { // Borrowed Inst same as Recall Requesting Inst
                            requestItemEntity.setRequestStatusId(requestStatusEntity.getId());
                            requestItemEntity.setLastUpdatedDate(new Date());
                            requestItemEntityRecalled.setRequestStatusId(requestStatusEntity.getId());
                            requestItemEntityRecalled.setLastUpdatedDate(new Date());
                            requestItemDetailsRepository.save(requestItemEntity);
                            requestItemDetailsRepository.save(requestItemEntityRecalled);
                            rollbackUpdateItemAvailabilityStatus(requestItemEntity.getItemEntity(), ScsbConstants.GUEST_USER);
                            itemRequestServiceUtil.updateSolrIndex(requestItemEntity.getItemEntity());
                            itemRefileResponse.setSuccess(true);
                            itemRefileResponse.setScreenMessage("Successfully Refiled");
                        } else { // Borrowed Inst not same as Recall Requesting Inst, Change Retrieval Order Status to Refiled.
                            requestItemEntity.setRequestStatusId(requestStatusEntity.getId());
                            requestItemDetailsRepository.save(requestItemEntity);
                            // Checkout the item based on the institution for the Recall order
                            if(itemRequestInfo.getRequestType().equalsIgnoreCase(ScsbConstants.EDD_REQUEST)) {
                                //Checkout for EDD patron
                                itemRequestInfo.setPatronBarcode(itemRequestServiceUtil.getPatronIdBorrowingInstitution(itemRequestInfo.getRequestingInstitution() ,itemRequestInfo.getItemOwningInstitution(), ScsbCommonConstants.REQUEST_TYPE_EDD));
                                requestItemController.checkoutItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                            } else {
                                logger.info("Retrieval Requesting Institution: {}", requestItemEntity.getInstitutionEntity().getInstitutionCode());
                                logger.info("Retrieval Item Owning Institution: {}", requestItemEntity.getItemEntity().getInstitutionEntity().getInstitutionCode());
                                logger.info("Retrieval Patron Barcode: {}", requestItemEntity.getPatronId());
                                logger.info("Recall Requesting Institution: {}", requestItemEntityRecalled.getInstitutionEntity().getInstitutionCode());
                                logger.info("Recall Item Owning Institution: {}", requestItemEntityRecalled.getItemEntity().getInstitutionEntity().getInstitutionCode());
                                logger.info("Recall Patron Barcode: {}", requestItemEntityRecalled.getPatronId());
                                logger.info("ItemRequestInfo - Req Ins - {}, Item Ins - {}, Patron - {}", itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getItemOwningInstitution(), itemRequestInfo.getPatronBarcode());
                                if (!requestItemEntityRecalled.getInstitutionEntity().getInstitutionCode().equalsIgnoreCase(requestItemEntityRecalled.getItemEntity().getInstitutionEntity().getInstitutionCode())) {
                                    itemRequestInfo.setPatronBarcode(itemRequestServiceUtil.getPatronIdBorrowingInstitution(requestItemEntityRecalled.getInstitutionEntity().getInstitutionCode(), requestItemEntityRecalled.getItemEntity().getInstitutionEntity().getInstitutionCode(), ScsbCommonConstants.REQUEST_TYPE_RETRIEVAL));
                                } else {
                                    itemRequestInfo.setPatronBarcode(requestItemEntityRecalled.getPatronId());
                                }
                                requestItemController.checkoutItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                            }
                            itemRequestInfo.setRequestingInstitution(requestItemEntityRecalled.getInstitutionEntity().getInstitutionCode());
                            setItemRequestInfoForRequest(itemEntity, itemRequestInfo, requestItemEntityRecalled);
                            ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
                            // Put back the Recall order to LAS. On success from LAS, recall order is updated to retrieval.
                            itemRequestInfo.setImsLocationCode(itemEntity.getImsLocationEntity().getImsLocationCode());
                            logger.info("ItemRequestInfo Before Sending to GFA - Req Ins - {}, Item Ins - {}, Patron - {}", itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getItemOwningInstitution(), itemRequestInfo.getPatronBarcode());
                            updateScsbAndGfa(itemRequestInfo, itemInformationResponse, itemEntity);
                            itemRefileResponse.setSuccess(true);
                            itemRequestInfo.setRequestingInstitution(requestItemEntity.getInstitutionEntity().getInstitutionCode());
                        }
                    }
                    logger.info("Refile Request Id = {} Refile Barcode = {}", requestItemEntity.getId(), itemBarcode);
                    //TODO - Check if EDD and change Patron accordingly to checkIn in RequestingInstitution
                    if(itemRequestInfo.getRequestType().equalsIgnoreCase(ScsbConstants.EDD_REQUEST)){
                        if(itemRequestInfo.isOwningInstitutionItem()) {
                            itemRequestInfo.setPatronBarcode(itemEDDRequestService.getPatronIdForOwningInstitutionOnEdd(itemRequestInfo.getItemOwningInstitution()));
                        }else {
                            //Interchanging the arguments since the checkin call is made based on Requesting Institution.
                            itemRequestInfo.setPatronBarcode(itemRequestServiceUtil.getPatronIdBorrowingInstitution(itemRequestInfo.getItemOwningInstitution(),itemRequestInfo.getRequestingInstitution(), ScsbCommonConstants.REQUEST_TYPE_EDD));
                        }
                    }
                    else {
                        itemRequestInfo.setPatronBarcode(requestItemEntity.getPatronId());
                    }
                    String isRefileForCheckin = propertyUtil.getPropertyByInstitutionAndKey(itemRequestInfo.getRequestingInstitution(), PropertyKeyConstants.ILS.ILS_USE_REFILE_FOR_CHECKIN);
                    if(!itemRequestInfo.getRequestType().equalsIgnoreCase(ScsbConstants.EDD_REQUEST)) {
                        if (Boolean.TRUE.toString().equalsIgnoreCase(isRefileForCheckin)) {
                            requestItemController.getIlsProtocolConnectorFactory().getIlsProtocolConnector(itemRequestInfo.getRequestingInstitution()).refileItem(itemBarcode);
                        } else {
                            requestItemController.checkinItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                        }
                    }
                    if (!itemRequestInfo.isOwningInstitutionItem() || ((itemRequestInfo.getRequestType().equalsIgnoreCase(ScsbConstants.EDD_REQUEST))
                            && itemRequestInfo.isOwningInstitutionItem())) {
                        //TODO - Check if EDD and change Patron accordingly to checkIn in ItemOwningInstitution
                        if(itemRequestInfo.getRequestType().equalsIgnoreCase(ScsbConstants.EDD_REQUEST)){
                            itemRequestInfo.setPatronBarcode(getPatronIDForEDDBorrowingInstitution(itemRequestInfo.getRequestingInstitution(),itemRequestInfo.getItemOwningInstitution()));
                        }
                        else {
                            itemRequestInfo.setPatronBarcode(itemRequestServiceUtil.getPatronIdBorrowingInstitution(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getItemOwningInstitution(), ScsbCommonConstants.REQUEST_TYPE_RETRIEVAL));
                        }
                        requestItemController.checkinItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                    }
                }
                itemRefileResponse.setScreenMessage("Cannot refile a already available Item");
            }
        }
        else {
            itemRefileResponse.setScreenMessage("Cannot Refile.Please check the provided barcode(s) and requestId(s)");
            for (RequestItemEntity requestItemEntity : requestItemEntities) {
                if (requestItemEntity.getRequestStatusEntity().getRequestStatusCode().equalsIgnoreCase(ScsbConstants.LAS_REFILE_REQUEST_PLACED)) {
                    ItemRequestInformation itemRequestInfo = new ItemRequestInformation();
                    itemEntity = requestItemEntity.getItemEntity();
                    itemBarcode = itemEntity.getBarcode();
                    String gfaItemStatus = gfaLasService.callGfaItemStatus(itemBarcode);
                    logger.info("Gfa status received during refile : {}",gfaItemStatus);
                    logger.info("GFA Item Status {} for the barcode {} received on Refile where Request Id : {}", gfaItemStatus, itemEntity.getBarcode(),requestItemEntity.getId());
                    logger.info("Rejecting the Refile for the barcode {} where Request ID : {} and Request Status : {}", itemEntity.getBarcode(), requestItemEntity.getId(), requestItemEntity.getRequestStatusEntity().getRequestStatusCode());
                    gfaItemStatus = gfaLasService.getGfaItemStatusInUpperCase(gfaItemStatus);
                    logger.info("Gfa status After modifying : {}",gfaItemStatus);
                    boolean isImsItemStatusAvailable = commonUtil.checkIfImsItemStatusIsAvailableOrNotAvailable(itemEntity.getImsLocationEntity().getImsLocationCode(), gfaItemStatus, true);
                    logger.info("Condition satisfied {}", isImsItemStatusAvailable);
                    if (isImsItemStatusAvailable) {
                        itemRequestInfo.setItemBarcodes(Collections.singletonList(itemBarcode));
                        itemRequestInfo.setItemOwningInstitution(requestItemEntity.getItemEntity().getInstitutionEntity().getInstitutionCode());
                        itemRequestInfo.setRequestingInstitution(requestItemEntity.getInstitutionEntity().getInstitutionCode());
                        itemRequestInfo.setPatronBarcode(requestItemEntity.getPatronId());
                        setItemRequestInfoForRequest(itemEntity, itemRequestInfo, requestItemEntity);
                        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
                        itemRequestInfo.setImsLocationCode(itemEntity.getImsLocationEntity().getImsLocationCode());
                        updateScsbAndGfa(itemRequestInfo, itemInformationResponse, itemEntity);
                        logger.info("Successfully placed the request to Queue");
                        requestItemDetailsRepository.save(requestItemEntity);
                        itemRefileResponse.setSuccess(false);
                        itemRefileResponse.setScreenMessage("Refile ignored as request is placed after first scan and successfully placed request to queue");
                    }
                    else {
                        logger.info("Cannot Refile. LAS status is Not Available");
                    }
                }
            }
        }
        return itemRefileResponse;
    }

    private void setItemRequestInfoForRequest(ItemEntity itemEntity, ItemRequestInformation itemRequestInfo, RequestItemEntity requestItemEntity) {
        if(requestItemEntity.getRequestTypeEntity().getRequestTypeCode().equalsIgnoreCase(ScsbConstants.EDD_REQUEST)){
            String notes = requestItemEntity.getNotes();
            String[] eddInformation = notes.split("\n");
            HashMap<String,String> eddNotesMap= new HashMap<>();
            for (String eddInfo : eddInformation) {
                String[] eddInfoInPairs = eddInfo.split(":",2);
                if(!(eddInfoInPairs[0].isEmpty()))
                    eddNotesMap.put(eddInfoInPairs[0],eddInfoInPairs[1]);
            }
            if (itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId().trim().length() <= 0) {
                itemRequestInfo.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
            }
            SearchResultRow searchResultRow = searchRecords(itemEntity);
            itemRequestInfo.setTitleIdentifier(removeDiacritical(searchResultRow.getTitle().replaceAll("[^\\x00-\\x7F]", "?")));
            itemRequestInfo.setItemAuthor(removeDiacritical(searchResultRow.getAuthor()));
            itemRequestInfo.setEmailAddress(securityUtil.getDecryptedValue(requestItemEntity.getEmailId()));
            itemRequestInfo.setRequestType(ScsbConstants.EDD_REQUEST);
            if(itemRequestInfo.isOwningInstitutionItem()) {
                itemRequestInfo.setPatronBarcode(itemEDDRequestService.getPatronIdForOwningInstitutionOnEdd(itemRequestInfo.getItemOwningInstitution()));
            }
            else {
                itemRequestInfo.setPatronBarcode(itemRequestServiceUtil.getPatronIdBorrowingInstitution(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getItemOwningInstitution(), ScsbCommonConstants.REQUEST_TYPE_EDD));
            }
            setEddInformation(itemRequestInfo, eddNotesMap);
        }
        else {
            itemRequestInfo.setRequestType(ScsbCommonConstants.RETRIEVAL);
            if (null == requestItemEntity.getBulkRequestItemEntity()) {
                InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionCode(requestItemEntity.getInstitutionEntity().getInstitutionCode());
                DeliveryCodeEntity deliveryCodeEntity = deliveryCodeDetailsRepository.findByDeliveryCodeAndOwningInstitutionIdAndActive(requestItemEntity.getStopCode(), institutionEntity.getId(), 'Y');
                DeliveryCodeTranslationEntity deliveryCodeTranslationEntity = deliveryCodeTranslationDetailsRepository.findByRequestingInstitutionandImsLocation(institutionEntity.getId(), deliveryCodeEntity.getId(), itemEntity.getImsLocationEntity().getId());
                if (deliveryCodeTranslationEntity != null && deliveryCodeTranslationEntity.getImsLocationDeliveryCode() != null) {
                    logger.info("Translation Code >>>> {} "  , deliveryCodeTranslationEntity.getImsLocationDeliveryCode());
                    itemRequestInfo.setTranslatedDeliveryLocation(deliveryCodeTranslationEntity.getImsLocationDeliveryCode());
                }
            } else {
                itemRequestInfo.setTranslatedDeliveryLocation(requestItemEntity.getStopCode());
            }
        }
        itemRequestInfo.setRequestNotes(requestItemEntity.getNotes());
        itemRequestInfo.setRequestId(requestItemEntity.getId());
        itemRequestInfo.setUsername(requestItemEntity.getCreatedBy());
        itemRequestInfo.setDeliveryLocation(requestItemEntity.getStopCode());
        itemRequestInfo.setCustomerCode(itemEntity.getCustomerCode());
    }

    private void setEddInformation(ItemRequestInformation itemRequestInfo, HashMap<String, String> eddNotesMap) {
        for (Map.Entry<String, String> eddNotes : eddNotesMap.entrySet()) {
            if(eddNotes.getKey().contains("Start Page")){
                itemRequestInfo.setStartPage(eddNotes.getValue());
            }
            if(eddNotes.getKey().contains("End Page")){
                itemRequestInfo.setEndPage(eddNotes.getValue());
            }
            if(eddNotes.getKey().contains("Chapter")){
                itemRequestInfo.setChapterTitle(eddNotes.getValue());
            }
            if(eddNotes.getKey().contains("Article Author")){
                itemRequestInfo.setAuthor(eddNotes.getValue());
            }
            if(eddNotes.getKey().contains("Volume Number")){
                itemRequestInfo.setVolume(eddNotes.getValue());
            }
            if(eddNotes.getKey().contains("Article/Chapter Title")){
                itemRequestInfo.setChapterTitle(eddNotes.getValue());
            }
            if(eddNotes.getKey().contains("Issue")){
                itemRequestInfo.setIssue(eddNotes.getValue());
            }
            if(eddNotes.getKey().contains("User")){
                itemRequestInfo.setEddNotes(eddNotes.getValue());
            }
        }
    }

    /**
     * Send message to topic.
     *
     * @param owningInstituteId the owning institute id
     * @param requestType       the request type
     * @param itemResponseInfo  the item response info
     * @param exchange          the exchange
     */
    public void sendMessageToTopic(String owningInstituteId, String requestType, ItemInformationResponse itemResponseInfo, Exchange exchange) {
        String selectTopic = propertyUtil.getPropertyByInstitutionAndKey(owningInstituteId, PropertyKeyConstants.ILS.ILS_TOPIC_RETRIEVAL_REQUEST);
        if (requestType.equalsIgnoreCase(ScsbCommonConstants.REQUEST_TYPE_RETRIEVAL)) {
            selectTopic = propertyUtil.getPropertyByInstitutionAndKey(owningInstituteId, PropertyKeyConstants.ILS.ILS_TOPIC_RETRIEVAL_REQUEST);
        } else if (requestType.equalsIgnoreCase(ScsbCommonConstants.REQUEST_TYPE_EDD)) {
            selectTopic = propertyUtil.getPropertyByInstitutionAndKey(owningInstituteId, PropertyKeyConstants.ILS.ILS_TOPIC_EDD_REQUEST);
        } else if (requestType.equalsIgnoreCase(ScsbCommonConstants.REQUEST_TYPE_RECALL)) {
            selectTopic = propertyUtil.getPropertyByInstitutionAndKey(owningInstituteId, PropertyKeyConstants.ILS.ILS_TOPIC_RECALL_REQUEST);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";
        try {
            json = objectMapper.writeValueAsString(itemResponseInfo);
            logger.info("Topic logs : {}" , json);
        } catch (JsonProcessingException e) {
            logger.error(ScsbConstants.REQUEST_PARSE_EXCEPTION, e);
        }
        FluentProducerTemplate fluentProducerTemplate = DefaultFluentProducerTemplate.on(exchange.getContext());
        fluentProducerTemplate
                .to(selectTopic)
                .withBody(json);
        fluentProducerTemplate.send();
    }

    private ItemInformationResponse setItemResponseInformation(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInfo) {
        ItemInformationResponse itemResponseInformation = itemResponseInfo;
        itemResponseInformation.setRequestId(itemRequestInfo.getRequestId());
        itemResponseInformation.setDueDate(itemRequestInfo.getExpirationDate());
        itemResponseInformation.setBibID(itemRequestInfo.getBibId());
        itemResponseInformation.setItemOwningInstitution(itemRequestInfo.getItemOwningInstitution());
        itemResponseInformation.setRequestingInstitution(itemRequestInfo.getRequestingInstitution());
        itemResponseInformation.setPatronBarcode(itemRequestInfo.getPatronBarcode());
        itemResponseInformation.setRequestType(itemRequestInfo.getRequestType());
        itemResponseInformation.setEmailAddress(itemRequestInfo.getEmailAddress());
        itemResponseInformation.setDeliveryLocation(itemRequestInfo.getDeliveryLocation());
        itemResponseInformation.setRequestNotes(getNotes(itemResponseInformation.isSuccess(), itemResponseInformation.getScreenMessage(), itemRequestInfo.getRequestNotes()));
        itemResponseInformation.setItemBarcode(itemRequestInfo.getItemBarcodes().get(0));
        itemResponseInformation.setTitleIdentifier(itemRequestInfo.getTitleIdentifier());
        itemResponseInformation.setExpirationDate(itemRequestInfo.getExpirationDate());
        itemResponseInformation.setUsername(itemRequestInfo.getUsername());
        itemResponseInformation.setImsLocationCode(itemRequestInfo.getImsLocationCode());
        return itemResponseInformation;
    }

    /**
     * Update recap request item integer.
     *
     * @param itemRequestInformation the item request information
     * @param itemEntity             the item entity
     * @param requestStatusCode      the request status code
     * @return the integer
     */
    public Integer updateRecapRequestItem(ItemRequestInformation itemRequestInformation, ItemEntity itemEntity, String requestStatusCode) {
        return itemRequestDBService.updateRecapRequestItem(itemRequestInformation, itemEntity, requestStatusCode, null);
    }

    /**
     * Update recap request item item information response.
     *
     * @param itemInformationResponse the item information response
     * @return the item information response
     */
    public ItemInformationResponse updateRecapRequestItem(ItemInformationResponse itemInformationResponse) {
        return itemRequestDBService.updateRecapRequestItem(itemInformationResponse);
    }

    /**
     * Update recap request status item information response.
     *
     * @param itemInformationResponse the item information response
     * @return the item information response
     */
    public ItemInformationResponse updateRecapRequestStatus(ItemInformationResponse itemInformationResponse) {
        return itemRequestDBService.updateRecapRequestStatus(itemInformationResponse);
    }

    public boolean updateItemAvailabilityStatus(List<ItemEntity> itemEntities, String username) {
        ItemStatusEntity itemStatusEntity = itemStatusDetailsRepository.findByStatusCode(ScsbCommonConstants.NOT_AVAILABLE);
        for (ItemEntity itemEntity : itemEntities) {
            Optional<ItemEntity> optionalItemEntity = itemDetailsRepository.findById(itemEntity.getId());
             if(optionalItemEntity.isPresent()) {
                 ItemEntity itemEntityByItemId = optionalItemEntity.get();
                 logger.info("Item status : {}", itemEntityByItemId.getItemStatusEntity().getStatusCode());
                 if (itemStatusEntity.getId().equals(itemEntityByItemId.getItemAvailabilityStatusId())) {  //Condition should be checked with equals not == ?
                     return false;
                 }
             }
        }
        itemRequestDBService.updateItemAvailabilityStatus(itemEntities, username);
        return true;
    }

    public void rollbackUpdateItemAvailabilityStatus(ItemEntity itemEntity, String username) {
        commonUtil.rollbackUpdateItemAvailabilityStatus(itemEntity, username);
    }

    /**
     * Save item change log entity.
     *
     * @param recordId      the record id
     * @param userName      the user name
     * @param operationType the operation type
     * @param notes         the notes
     */
    public void saveItemChangeLogEntity(Integer recordId, String userName, String operationType, String notes) {
        commonUtil.saveItemChangeLogEntity(recordId, userName, operationType, notes);
    }

    /**
     * Gets user.
     *
     * @param userId the user id
     * @return the user
     */
    public String getUser(String userId) {
        return commonUtil.getUser(userId);
    }

    /**
     * Update gfa item information response.
     *
     * @param itemRequestInfo         the item request info
     * @param itemResponseInformation the item response information
     * @return the item information response
     */
    protected ItemInformationResponse updateGFA(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {

        try {
            if (itemRequestInfo.getRequestType().equalsIgnoreCase(ScsbCommonConstants.REQUEST_TYPE_RETRIEVAL) || itemRequestInfo.getRequestType().equalsIgnoreCase(ScsbCommonConstants.REQUEST_TYPE_EDD)) {
                itemResponseInformation = gfaLasService.executeRetrieveOrder(itemRequestInfo, itemResponseInformation);
            } else {
                itemResponseInformation.setSuccess(true);
                itemResponseInformation.setScreenMessage(ScsbConstants.RETRIEVAL_ORDER_NOT_REQUIRED_FOR_RECALL);
            }
        } catch (Exception e) {
            itemResponseInformation.setSuccess(false);
            itemResponseInformation.setScreenMessage(ScsbConstants.REQUEST_SCSB_EXCEPTION + e.getMessage());
            logger.error(ScsbCommonConstants.REQUEST_EXCEPTION, e);
        }
        return itemResponseInformation;
    }

    private ItemInformationResponse checkOwningInstitution(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity) {
        try {
            if (itemRequestInfo.isOwningInstitutionItem()) {
                itemResponseInformation = holdItem(itemRequestInfo.getItemOwningInstitution(), itemRequestInfo, itemResponseInformation, itemEntity);
            } else {// Not the Owning Institute
                // Get Temporary bibId from SCSB DB
                ItemCreateBibResponse createBibResponse;
                String isCreateBibEnabled = propertyUtil.getPropertyByInstitutionAndKey(itemRequestInfo.getRequestingInstitution(), PropertyKeyConstants.ILS.ILS_CREATE_BIB_API_ENABLED);
                if (Boolean.TRUE.toString().equalsIgnoreCase(isCreateBibEnabled)) {
                    createBibResponse = (ItemCreateBibResponse) requestItemController.createBibliogrphicItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                } else {
                    createBibResponse = new ItemCreateBibResponse();
                    createBibResponse.setSuccess(true);
                }
                if (createBibResponse.isSuccess()) {
                    itemRequestInfo.setBibId(createBibResponse.getBibId());
                    itemResponseInformation = holdItem(itemRequestInfo.getRequestingInstitution(), itemRequestInfo, itemResponseInformation, itemEntity);
                } else {
                    itemResponseInformation.setScreenMessage(ScsbConstants.REQUEST_ILS_EXCEPTION + ScsbConstants.CREATING_A_BIB_RECORD_FAILED_IN_ILS);
                    itemResponseInformation.setSuccess(createBibResponse.isSuccess());
                    rollbackUpdateItemAvailabilityStatus(itemEntity, itemRequestInfo.getUsername());
                    saveItemChangeLogEntity(itemEntity.getId(), getUser(itemRequestInfo.getUsername()), ScsbConstants.REQUEST_ITEM_HOLD_FAILURE, createBibResponse.getBibId() + " - " + createBibResponse.getScreenMessage());
                }
            }
        } catch (Exception e) {
            logger.error(ScsbCommonConstants.REQUEST_EXCEPTION, e);
            itemResponseInformation.setScreenMessage(ScsbConstants.REQUEST_SCSB_EXCEPTION + e.getMessage());
            itemResponseInformation.setSuccess(false);
            saveItemChangeLogEntity(itemEntity.getId(), getUser(itemRequestInfo.getUsername()), ScsbConstants.REQUEST_ITEM_ITEM_CHANGE_LOG_EXCEPTION, itemRequestInfo.getItemBarcodes() + " - " + e.getMessage());
        }
        return itemResponseInformation;
    }

    private ItemInformationResponse checkInstAfterPlacingRequest(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity) {
        if (itemRequestInfo.isOwningInstitutionItem()) {
            itemResponseInformation = updateScsbAndGfa(itemRequestInfo, itemResponseInformation, itemEntity);
        } else { // Item does not belong to requesting Institute
            itemResponseInformation = updateScsbAndGfa(itemRequestInfo, itemResponseInformation, itemEntity);
            logger.info("GFA Response for Retrieval request : {}",itemResponseInformation.isSuccess());
            if(itemResponseInformation.isSuccess()){
                try {
                    String useGenericPatronRetrievalForCross = propertyUtil.getPropertyByInstitutionAndKey(itemRequestInfo.getRequestingInstitution(), PropertyKeyConstants.ILS.ILS_USE_GENERIC_PATRON_RETRIEVAL_CROSS);
                    if (Boolean.TRUE.toString().equalsIgnoreCase(useGenericPatronRetrievalForCross)) {
                        try {
                            itemRequestInfo.setPatronBarcode(itemRequestServiceUtil.getPatronIdBorrowingInstitution(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getItemOwningInstitution(), ScsbCommonConstants.REQUEST_TYPE_RETRIEVAL));
                        } catch (Exception e) {
                            logger.error(ScsbCommonConstants.REQUEST_EXCEPTION, e);
                            itemRequestInfo.setPatronBarcode("");
                        }
                    }
                    logger.info("Performing CheckOut using the generic patron : {} in Owning Institution : {}",itemRequestInfo.getPatronBarcode(), itemRequestInfo.getItemOwningInstitution());
                    requestItemController.checkoutItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                } catch (Exception e) {
                    logger.error(ScsbCommonConstants.REQUEST_EXCEPTION, e);
                    logger.error("Cross Partner Request Item Checkout Failed. Own Ins: {}, Req Ins: {}, Cross PatronId: {}", itemResponseInformation.getItemOwningInstitution(), itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getPatronBarcode());
                }
            }
        }
        if (itemResponseInformation.isSuccess()) {
            itemRequestServiceUtil.updateSolrIndex(itemEntity);
        }
        return itemResponseInformation;
    }

    private ItemInformationResponse holdItem(String callingInst, ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity) {
        ItemHoldResponse itemHoldResponse = (ItemHoldResponse) requestItemController.holdItem(itemRequestInfo, callingInst);
        if (itemHoldResponse.isSuccess()) { // IF Hold command is successfully
            itemResponseInformation.setExpirationDate(itemHoldResponse.getExpirationDate());
            itemRequestInfo.setExpirationDate(itemHoldResponse.getExpirationDate());
            itemResponseInformation = checkInstAfterPlacingRequest(itemRequestInfo, itemResponseInformation, itemEntity);
        } else { // If Hold command Failure
            itemResponseInformation.setScreenMessage(ScsbConstants.REQUEST_ILS_EXCEPTION + itemHoldResponse.getScreenMessage());
            itemResponseInformation.setSuccess(itemHoldResponse.isSuccess());
            rollbackUpdateItemAvailabilityStatus(itemEntity, itemRequestInfo.getUsername());
            saveItemChangeLogEntity(itemEntity.getId(), getUser(itemRequestInfo.getUsername()), ScsbConstants.REQUEST_ITEM_HOLD_FAILURE, itemHoldResponse.getPatronIdentifier() + " - " + itemHoldResponse.getScreenMessage());
        }
        return itemResponseInformation;
    }

    private ItemInformationResponse updateScsbAndGfa(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity) {
        Integer requestId = 0;
        if (gfaLasService.isUseQueueLasCall(itemRequestInfo.getImsLocationCode())) {
            requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, ScsbConstants.REQUEST_STATUS_PENDING);
        }
        itemResponseInformation.setRequestId(requestId);
        itemResponseInformation = updateGFA(itemRequestInfo, itemResponseInformation);
        if(itemResponseInformation.isRequestTypeForScheduledOnWO()){
            logger.info("Request Received on first scan");
            requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, ScsbConstants.LAS_REFILE_REQUEST_PLACED);
            logger.info("Updated the request id {} on first scan",requestId);
        }
        if (itemResponseInformation.isSuccess()) {
            itemResponseInformation.setScreenMessage(ScsbConstants.SUCCESSFULLY_PROCESSED_REQUEST_ITEM);
        } else {
            rollbackAfterGFA(itemEntity, itemRequestInfo, itemResponseInformation);
        }
        return itemResponseInformation;
    }

    private ItemInformationResponse checkOwningInstitutionRecall(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity) {
        String messagePublish;
        boolean bsuccess;
        RequestItemEntity requestItemEntity = requestItemDetailsRepository.findByItemBarcodeAndRequestStaCode(itemRequestInfo.getItemBarcodes().get(0), ScsbCommonConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
        logger.info("Owning     Inst = {}" , requestItemEntity.getItemEntity().getInstitutionEntity().getInstitutionCode());
        logger.info("Borrowed   Inst = {}" , requestItemEntity.getInstitutionEntity().getInstitutionCode());
        logger.info("Requesting Inst = {}" , itemRequestInfo.getRequestingInstitution());
        String instToGetItemInfo = requestItemEntity.getInstitutionEntity().getInstitutionCode();
        ItemInformationResponse itemInformation = (ItemInformationResponse) requestItemController.itemInformation(itemRequestInfo, instToGetItemInfo);
        String checkedOutCirculationStatuses = propertyUtil.getPropertyByInstitutionAndKey(instToGetItemInfo, PropertyKeyConstants.ILS.ILS_CHECKEDOUT_CIRCULATION_STATUS);
        if (StringUtils.isNotBlank(checkedOutCirculationStatuses) && StringUtils.containsIgnoreCase(checkedOutCirculationStatuses, itemInformation.getCirculationStatus())) {
            if (requestItemEntity.getInstitutionEntity().getInstitutionCode().equalsIgnoreCase(itemRequestInfo.getRequestingInstitution())) {
                ItemRecallResponse itemRecallResponse = (ItemRecallResponse) requestItemController.recallItem(itemRequestInfo, requestItemEntity.getInstitutionEntity().getInstitutionCode());
                if (itemRecallResponse.isSuccess()) {
                    // Update Recap DB
                    itemRequestInfo.setExpirationDate(itemRecallResponse.getExpirationDate());
                    sendEmail(requestItemEntity.getItemEntity().getCustomerCode(), itemRequestInfo.getItemBarcodes().get(0), itemRequestInfo.getImsLocationCode(), itemRequestInfo.getPatronBarcode(), requestItemEntity.getInstitutionEntity().getInstitutionCode());
                    messagePublish = ScsbConstants.SUCCESSFULLY_PROCESSED_REQUEST_ITEM;
                    bsuccess = true;
                } else {
                    messagePublish = recallError(itemRecallResponse);
                    bsuccess = false;
                }
            } else {
                createBibAndHold(itemRequestInfo, itemResponseInformation, itemEntity);
                if (itemResponseInformation.isSuccess()) { // IF Hold command is successfully
                    itemRequestInfo.setExpirationDate(itemRequestInfo.getExpirationDate());
                    String requestingPatron = itemRequestInfo.getPatronBarcode();
                    String useGenericPatronRetrievalForCross = propertyUtil.getPropertyByInstitutionAndKey(itemRequestInfo.getRequestingInstitution(), PropertyKeyConstants.ILS.ILS_USE_GENERIC_PATRON_RETRIEVAL_CROSS);
                    ItemRecallResponse itemRecallResponse = new ItemRecallResponse();
                    if (Boolean.TRUE.toString().equalsIgnoreCase(useGenericPatronRetrievalForCross)) {
                        try {
                            itemRequestInfo.setPatronBarcode(itemRequestServiceUtil.getPatronIdBorrowingInstitution(itemRequestInfo.getRequestingInstitution(), requestItemEntity.getInstitutionEntity().getInstitutionCode(), ScsbCommonConstants.REQUEST_TYPE_RETRIEVAL));
                            InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionCode(itemRequestInfo.getRequestingInstitution());
                            itemRequestInfo.setPickupLocation(getPickupLocation(institutionEntity.getId(), requestItemEntity.getStopCode()));
                            itemRequestInfo.setBibId(itemInformation.getBibID());
                            itemRecallResponse = (ItemRecallResponse) requestItemController.recallItem(itemRequestInfo, requestItemEntity.getInstitutionEntity().getInstitutionCode());
                        } catch (Exception e) {
                            logger.error(ScsbCommonConstants.REQUEST_EXCEPTION, e);
                            itemRecallResponse.setSuccess(false);
                            itemRecallResponse.setScreenMessage(ScsbConstants.GENERIC_PATRON_NOT_FOUND_ERROR);
                        }
                    }
                    itemRequestInfo.setPatronBarcode(requestingPatron);
                    if (itemRecallResponse.isSuccess()) {
                        sendEmail(requestItemEntity.getItemEntity().getCustomerCode(), itemRequestInfo.getItemBarcodes().get(0), itemRequestInfo.getImsLocationCode(), itemRequestInfo.getPatronBarcode(), requestItemEntity.getInstitutionEntity().getInstitutionCode());
                        messagePublish = ScsbConstants.SUCCESSFULLY_PROCESSED_REQUEST_ITEM;
                        bsuccess = true;
                    } else {
                        messagePublish = recallError(itemRecallResponse);
                        bsuccess = false;
                        requestItemController.cancelHoldItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                        saveItemChangeLogEntity(itemEntity.getId(), getUser(itemRequestInfo.getUsername()), ScsbConstants.REQUEST_ITEM_HOLD_FAILURE, itemRequestInfo.getPatronBarcode() + " - " + itemResponseInformation.getScreenMessage());
                    }
                } else { // If Hold command Failure
                    messagePublish = ScsbConstants.REQUEST_ILS_EXCEPTION + itemResponseInformation.getScreenMessage();
                    bsuccess = false;
                }
            }
        } else {
            messagePublish = ScsbConstants.REQUEST_SCSB_EXCEPTION + ScsbConstants.RECALL_CANNOT_BE_PROCESSED_THE_ITEM_IS_NOT_CHECKED_OUT_IN_ILS;
            bsuccess = false;
        }
        itemResponseInformation.setScreenMessage(messagePublish);
        itemResponseInformation.setSuccess(bsuccess);
        return itemResponseInformation;
    }

    private String recallError(ItemRecallResponse itemRecallResponse) {
        if (itemRecallResponse.getScreenMessage() != null && itemRecallResponse.getScreenMessage().trim().length() > 0) {
            return ScsbConstants.REQUEST_SCSB_EXCEPTION + itemRecallResponse.getScreenMessage();
        } else {
            return ScsbConstants.REQUEST_SCSB_EXCEPTION + ScsbConstants.RECALL_FAILED_NO_MESSAGE_RETURNED;
        }
    }

    private String getPatronIDForEDDBorrowingInstitution(String requestingInstitution, String owningInstitution) {
        GenericPatronEntity genericPatronEntity = genericPatronDetailsRepository.findByRequestingInstitutionCodeAndItemOwningInstitutionCode(requestingInstitution, owningInstitution);
        logger.info(genericPatronEntity.getEddGenericPatron());
        return genericPatronEntity.getEddGenericPatron();
    }

    private ItemInformationResponse createBibAndHold(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity) {
        ItemCreateBibResponse createBibResponse;
        String isCreateBibEnabled = propertyUtil.getPropertyByInstitutionAndKey(itemRequestInfo.getRequestingInstitution(), PropertyKeyConstants.ILS.ILS_CREATE_BIB_API_ENABLED);
        if (Boolean.TRUE.toString().equalsIgnoreCase(isCreateBibEnabled)) {
            createBibResponse = (ItemCreateBibResponse) requestItemController.createBibliogrphicItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
        } else {
            createBibResponse = new ItemCreateBibResponse();
            createBibResponse.setSuccess(true);
        }
        if (createBibResponse.isSuccess()) {
            itemRequestInfo.setBibId(createBibResponse.getBibId());
            ItemHoldResponse itemHoldResponse = (ItemHoldResponse) requestItemController.holdItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
            itemResponseInformation.setScreenMessage(itemHoldResponse.getScreenMessage());
            itemResponseInformation.setSuccess(itemHoldResponse.isSuccess());
        } else {
            itemResponseInformation.setScreenMessage(ScsbConstants.REQUEST_ILS_EXCEPTION + ScsbConstants.CREATING_A_BIB_RECORD_FAILED_IN_ILS);
            itemResponseInformation.setSuccess(createBibResponse.isSuccess());
            if (itemRequestInfo.getRequestType().equalsIgnoreCase(ScsbCommonConstants.REQUEST_TYPE_RETRIEVAL)) {
                rollbackUpdateItemAvailabilityStatus(itemEntity, itemRequestInfo.getUsername());
            }
        }
        return itemResponseInformation;
    }

    /**
     * Search records search result row.
     *
     * @param itemEntity the item entity
     * @return the search result row
     */
    public SearchResultRow searchRecords(ItemEntity itemEntity) {
        List<SearchResultRow> statusResponse;
        SearchResultRow searchResultRow = null;
        try {
            HttpEntity requestEntity = new HttpEntity<>(getRestHeaderService().getHttpHeaders());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(scsbSolrClientUrl + ScsbConstants.SEARCH_RECORDS_SOLR)
                    .queryParam(ScsbConstants.SEARCH_RECORDS_SOLR_PARAM_FIELD_NAME, ScsbConstants.SEARCH_RECORDS_SOLR_PARAM_FIELD_NAME_VALUE)
                    .queryParam(ScsbConstants.SEARCH_RECORDS_SOLR_PARAM_FIELD_VALUE, itemEntity.getBarcode());
            ResponseEntity<List<SearchResultRow>> responseEntity = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<SearchResultRow>>() {});
            statusResponse = responseEntity.getBody();
            if (statusResponse != null && !statusResponse.isEmpty()) {
                searchResultRow = statusResponse.get(0);
            }
        } catch (Exception e) {
            logger.error(ScsbCommonConstants.REQUEST_EXCEPTION, e);
        }
        return searchResultRow;
    }

    /**
     * Gets title.
     *
     * @param title           the title
     * @param itemEntity      the item entity
     * @param searchResultRow the search result row
     * @return the title
     */
    public String getTitle(String title, ItemEntity itemEntity, SearchResultRow searchResultRow) {
        String titleIdentifier = "";
        String useRestrictions = ScsbConstants.REQUEST_USE_RESTRICTIONS;
        String imsLocationCode = "";
        String lTitle;
        String returnTitle = "";
        try {
            if (itemEntity != null) {
                imsLocationCode = itemEntity.getImsLocationEntity().getImsLocationCode();
                if (StringUtils.isNotBlank(itemEntity.getUseRestrictions())) {
                    useRestrictions = itemEntity.getUseRestrictions();
                }
            }
            if (!(title != null && title.trim().length() > 0)) {
                if (searchResultRow != null) {
                    lTitle = searchResultRow.getTitle();
                } else {
                    lTitle = "";
                }
            } else {
                lTitle = title;
            }
            if (lTitle != null && lTitle.trim().length() > 126) {
                lTitle = lTitle.toUpperCase().substring(0, 126);
            } else if (lTitle != null && lTitle.trim().length() <= 0) {
                lTitle = "";
            }
            if (lTitle != null) {
                titleIdentifier = String.format("[%s] %s [%s]", useRestrictions, lTitle.toUpperCase(), imsLocationCode);
            }
            returnTitle = removeDiacritical(titleIdentifier);
            logger.info(returnTitle);
        } catch (Exception e) {
            logger.error(ScsbCommonConstants.REQUEST_EXCEPTION, e);
        }
        return returnTitle;
    }

    private void rollbackAfterGFA(ItemEntity itemEntity, ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {
        if (!itemResponseInformation.getScreenMessage().equalsIgnoreCase(ScsbConstants.GFA_ITEM_STATUS_CHECK_FAILED)) {
            rollbackUpdateItemAvailabilityStatus(itemEntity, itemRequestInfo.getUsername());
            saveItemChangeLogEntity(itemEntity.getId(), getUser(itemRequestInfo.getUsername()), ScsbConstants.REQUEST_ITEM_GFA_FAILURE, itemRequestInfo.getPatronBarcode() + " - " + itemResponseInformation.getScreenMessage());
        }

        String isCheckinInstitution = propertyUtil.getPropertyByInstitutionAndKey(itemRequestInfo.getRequestingInstitution(), PropertyKeyConstants.ILS.ILS_CHECKIN_INSTITUTION);
        String isEmailOnlyInstitution = propertyUtil.getPropertyByInstitutionAndKey(itemRequestInfo.getRequestingInstitution(), PropertyKeyConstants.ILS.LAS_EXCEPTION_EMAIL_ONLY);

        if (Boolean.TRUE.toString().equalsIgnoreCase(isCheckinInstitution) && itemRequestInfo.isOwningInstitutionItem()) {
        //DO NOTHING
        }
        else if (Boolean.TRUE.toString().equalsIgnoreCase(isCheckinInstitution) && !itemRequestInfo.isOwningInstitutionItem()) {
            if(Boolean.FALSE.toString().equalsIgnoreCase(isEmailOnlyInstitution)) {
                requestItemController.checkinItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
            }
            sendLASExceptionEmail(itemEntity.getCustomerCode(), itemEntity.getBarcode(), itemRequestInfo.getPatronBarcode(), itemRequestInfo.getItemOwningInstitution());
        }
        else {
            requestItemController.cancelHoldItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
            sendLASExceptionEmail(itemEntity.getCustomerCode(), itemEntity.getBarcode(), itemRequestInfo.getPatronBarcode(), itemRequestInfo.getItemOwningInstitution());
        }
    }

    private void rollbackAfterGFA(ItemInformationResponse itemResponseInformation) {
        ItemRequestInformation itemRequestInformation = itemRequestDBService.rollbackAfterGFA(itemResponseInformation);
        Optional<RequestItemEntity> requestItemEntity = requestItemDetailsRepository.findById(itemResponseInformation.getRequestId());
        if (requestItemEntity.isPresent()) {
            itemRequestServiceUtil.updateSolrIndex(requestItemEntity.get().getItemEntity());
        }
        if (itemResponseInformation.isBulk()) {
            requestItemController.checkinItem(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
        } else {
            String isCheckinInstitution = propertyUtil.getPropertyByInstitutionAndKey(itemRequestInformation.getRequestingInstitution(), PropertyKeyConstants.ILS.ILS_CHECKIN_INSTITUTION);
            String isEmailOnlyInstitution = propertyUtil.getPropertyByInstitutionAndKey(itemRequestInformation.getRequestingInstitution(), PropertyKeyConstants.ILS.LAS_EXCEPTION_EMAIL_ONLY);

            if (Boolean.TRUE.toString().equalsIgnoreCase(isCheckinInstitution)) {
                if(!itemRequestInformation.isOwningInstitutionItem()) {
                    if(Boolean.FALSE.toString().equalsIgnoreCase(isEmailOnlyInstitution)) {
                        requestItemController.checkinItem(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
                    }
                    if (requestItemEntity.isPresent()) {
                        sendLASExceptionEmail(requestItemEntity.get().getItemEntity().getCustomerCode(), requestItemEntity.get().getItemEntity().getBarcode(), requestItemEntity.get().getPatronId(), requestItemEntity.get().getInstitutionEntity().getInstitutionCode());
                    }

                }
            }
            else {
                requestItemController.cancelHoldItem(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
                if (requestItemEntity.isPresent()) {
                    sendLASExceptionEmail(requestItemEntity.get().getItemEntity().getCustomerCode(), requestItemEntity.get().getItemEntity().getBarcode(), requestItemEntity.get().getPatronId(), requestItemEntity.get().getInstitutionEntity().getInstitutionCode());
                }
            }
        }
        logger.info("Send LAS Status eMail");
        logger.info("Send LAS Status eMail Done");
 }

    /**
     * Gets notes.
     *
     * @param success       the success
     * @param screenMessage the screen message
     * @param userNotes     the user notes
     * @return the notes
     */
    protected String getNotes(boolean success, String screenMessage, String userNotes) {
        String notes = "";
        if (!StringUtils.isBlank(userNotes)) {
            notes = String.format("User: %s", userNotes);
        }
        if (!success && !StringUtils.isBlank(screenMessage)) {
            if (!StringUtils.isBlank(notes)) {
                notes += "\n";
            }
            notes += screenMessage;
        }
        return notes;
    }

    /**
     * Process las retrieve response.
     *
     * @param body the body
     */
    public void processLASRetrieveResponse(String body) {
        ItemInformationResponse itemInformationResponse = gfaLasService.processLASRetrieveResponse(body);
        itemInformationResponse = updateRecapRequestStatus(itemInformationResponse);
        if (!itemInformationResponse.isSuccess()) {
            rollbackAfterGFA(itemInformationResponse);
        }
    }

    /**
     * @param body
     */
    public void processLASEddRetrieveResponse(String body) {
        ItemInformationResponse itemInformationResponse = gfaLasService.processLASEDDRetrieveResponse(body);
        if (itemInformationResponse.isSuccess()) {
            updateRecapRequestStatus(itemInformationResponse);
        } else {
            updateRecapRequestStatus(itemInformationResponse);
            rollbackAfterGFA(itemInformationResponse);
        }
    }

    /**
     * @param text
     * @return
     */
    public String removeDiacritical(String text) {
        return text == null ? null : Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    private void sendEmail(String customerCode, String itemBarcode, String imsLocationCode, String patronBarcode, String toInstitution) {
        emailService.sendEmail(customerCode, itemBarcode, imsLocationCode, ScsbConstants.REQUEST_RECALL_TO_BORRWER, patronBarcode, toInstitution, ScsbConstants.REQUEST_RECALL_SUBJECT);
    }

    private void sendLASExceptionEmail(String customerCode, String itemBarcode, String patronBarcode, String toInstitution) {
        emailService.sendLASExceptionEmail(customerCode, itemBarcode, ScsbConstants.ITEM_STATUS_NOT_AVAILABLE, patronBarcode, toInstitution, ScsbConstants.GFA_RETRIVAL_ITEM_NOT_AVAILABLE);
    }

    private String getPickupLocation(Integer institutionId, String deliveryLocation) {
        DeliveryCodeEntity deliveryCodeEntity = deliveryCodeDetailsRepository.findByDeliveryCodeAndOwningInstitutionIdAndActive(deliveryLocation, institutionId, 'Y');
        if (deliveryCodeEntity != null) {
            return deliveryCodeEntity.getPickupLocation() != null ? deliveryCodeEntity.getPickupLocation() : "";
        }
        else {
            return "";
       }
    }

    /**
     * Is use queue las call boolean.
     *
     * @return the boolean
     */
    public boolean isUseQueueLasCall(String imsLocationCode) {
        return Boolean.parseBoolean(this.propertyUtil.getPropertyByImsLocationAndKey(imsLocationCode, PropertyKeyConstants.IMS.IMS_USE_QUEUE));
    }

    public boolean executeLasitemCheck(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {
        RequestStatusEntity requestStatusEntity = null;
        Optional<RequestItemEntity> requestItemEntity = requestItemDetailsRepository.findById(itemRequestInfo.getRequestId());
        itemResponseInformation = gfaLasService.executeRetrieveOrder(itemRequestInfo, itemResponseInformation);
        logger.info("itemResponseInformation-> {}" , itemResponseInformation.isSuccess());
        if (itemResponseInformation.isSuccess()) {
            requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ScsbConstants.REQUEST_STATUS_PENDING);
            if(requestItemEntity.isPresent()) {
                requestItemEntity.get().setRequestStatusId(requestStatusEntity.getId());
                requestItemEntity.get().setLastUpdatedDate(new Date());
                requestItemDetailsRepository.save(requestItemEntity.get());
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Replaces the requests to LAS Queue.
     * @param replaceRequest
     * @return
     */
    public Map<String, String> replaceRequestsToLASQueue(ReplaceRequest replaceRequest) {
        Map<String, String> resultMap = new HashMap<>();
        String replaceRequestByType = replaceRequest.getReplaceRequestByType();
        try {
            if (StringUtils.isNotBlank(replaceRequestByType)) {
                resultMap = replaceRequestToLASQueueByType(replaceRequest, replaceRequestByType);
            } else {
                resultMap.put(ScsbCommonConstants.INVALID_REQUEST, ScsbConstants.REQUEST_REPLACE_BY_TYPE_NOT_SELECTED);
            }
        } catch (Exception exception) {
            logger.error(ScsbCommonConstants.REQUEST_EXCEPTION, exception);
            resultMap.put(ScsbCommonConstants.FAILURE, exception.getMessage());
        }
        return resultMap;
    }

    /**
     * Replaces the requests to LAS Queue based on the replace request type.
     * @param replaceRequest
     * @param replaceRequestByType
     * @return
     * @throws ParseException
     */
    private Map<String, String> replaceRequestToLASQueueByType(ReplaceRequest replaceRequest, String replaceRequestByType) throws ParseException {
        Map<String, String> resultMap = new HashMap<>();
        if (ScsbCommonConstants.REQUEST_STATUS.equalsIgnoreCase(replaceRequestByType)) {
            String requestStatus = replaceRequest.getRequestStatus();
            if (StringUtils.isNotBlank(requestStatus)) {
                if (ScsbConstants.REQUEST_STATUS_PENDING.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.findByRequestStatusCode(Collections.singletonList(ScsbConstants.REQUEST_STATUS_PENDING));
                    resultMap = buildRequestInfoAndReplaceToLAS(requestItemEntities);
                } else if (ScsbConstants.REQUEST_STATUS_LAS_ITEM_STATUS_PENDING.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.findByRequestStatusCode(Collections.singletonList(ScsbConstants.REQUEST_STATUS_LAS_ITEM_STATUS_PENDING));
                    resultMap = buildRequestInfoAndReplaceToLAS(requestItemEntities);
                } else if (ScsbConstants.REQUEST_STATUS_EXCEPTION.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.findByRequestStatusCode(Collections.singletonList(ScsbConstants.REQUEST_STATUS_EXCEPTION));
                    resultMap = buildRequestInfoAndReplaceToSCSB(requestItemEntities);
                } else {
                    resultMap.put(ScsbCommonConstants.INVALID_REQUEST, ScsbConstants.REQUEST_STATUS_INVALID);
                }
            } else {
                resultMap.put(ScsbCommonConstants.INVALID_REQUEST, ScsbConstants.REQUEST_STATUS_INVALID);
            }
        } else if (ScsbCommonConstants.REQUEST_IDS.equalsIgnoreCase(replaceRequestByType)) {
            if (StringUtils.isNotBlank(replaceRequest.getRequestIds()) && StringUtils.isNotBlank(replaceRequest.getRequestStatus())) {
                String requestStatus = replaceRequest.getRequestStatus();
                List<Integer> requestIds = new ArrayList<>();
                Arrays.stream(replaceRequest.getRequestIds().split(",")).forEach(requestId -> requestIds.add(Integer.valueOf(requestId.trim())));
                resultMap.put(ScsbCommonConstants.TOTAL_REQUESTS_IDS, String.valueOf(requestIds.size()));
                if (ScsbConstants.REQUEST_STATUS_PENDING.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.findByIdsAndStatusCodes(requestIds, Collections.singletonList(ScsbConstants.REQUEST_STATUS_PENDING));
                    resultMap = buildRequestInfoAndReplaceToLAS(requestItemEntities);
                } else if (ScsbConstants.REQUEST_STATUS_LAS_ITEM_STATUS_PENDING.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.findByIdsAndStatusCodes(requestIds, Collections.singletonList(ScsbConstants.REQUEST_STATUS_LAS_ITEM_STATUS_PENDING));
                    resultMap = buildRequestInfoAndReplaceToLAS(requestItemEntities);
                } else if (ScsbConstants.REQUEST_STATUS_EXCEPTION.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.findByIdsAndStatusCodes(requestIds, Collections.singletonList(ScsbConstants.REQUEST_STATUS_EXCEPTION));
                    resultMap = buildRequestInfoAndReplaceToSCSB(requestItemEntities);
                } else {
                    resultMap.put(ScsbCommonConstants.INVALID_REQUEST, ScsbConstants.REQUEST_STATUS_INVALID);
                }
            } else {
                resultMap.put(ScsbCommonConstants.INVALID_REQUEST, ScsbConstants.REQUEST_IDS_INVALID);
            }
        } else if (ScsbConstants.REQUEST_IDS_RANGE.equalsIgnoreCase(replaceRequestByType)) {
            if (StringUtils.isNotBlank(replaceRequest.getStartRequestId()) && StringUtils.isNotBlank(replaceRequest.getEndRequestId()) && StringUtils.isNotBlank(replaceRequest.getRequestStatus())) {
                String requestStatus = replaceRequest.getRequestStatus();
                Integer startRequestId = Integer.valueOf(replaceRequest.getStartRequestId());
                Integer endRequestId = Integer.valueOf(replaceRequest.getEndRequestId());
                if (ScsbConstants.REQUEST_STATUS_PENDING.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.getRequestsBasedOnRequestIdRangeAndRequestStatusCode(startRequestId, endRequestId, ScsbConstants.REQUEST_STATUS_PENDING);
                    resultMap = buildRequestInfoAndReplaceToLAS(requestItemEntities);
                } else if (ScsbConstants.REQUEST_STATUS_LAS_ITEM_STATUS_PENDING.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.getRequestsBasedOnRequestIdRangeAndRequestStatusCode(startRequestId, endRequestId, ScsbConstants.REQUEST_STATUS_LAS_ITEM_STATUS_PENDING);
                    resultMap = buildRequestInfoAndReplaceToLAS(requestItemEntities);
                } else if (ScsbConstants.REQUEST_STATUS_EXCEPTION.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.getRequestsBasedOnRequestIdRangeAndRequestStatusCode(startRequestId, endRequestId, ScsbConstants.REQUEST_STATUS_EXCEPTION);
                    resultMap = buildRequestInfoAndReplaceToSCSB(requestItemEntities);
                } else {
                    resultMap.put(ScsbCommonConstants.INVALID_REQUEST, ScsbConstants.REQUEST_STATUS_INVALID);
                }
            } else {
                resultMap.put(ScsbCommonConstants.INVALID_REQUEST, ScsbConstants.REQUEST_START_END_IDS_INVALID);
            }
        } else if (ScsbConstants.REQUEST_DATES_RANGE.equalsIgnoreCase(replaceRequestByType)) {
            if (StringUtils.isNotBlank(replaceRequest.getFromDate()) && StringUtils.isNotBlank(replaceRequest.getToDate()) && StringUtils.isNotBlank(replaceRequest.getRequestStatus())) {
                String requestStatus = replaceRequest.getRequestStatus();
                SimpleDateFormat dateFormatter = new SimpleDateFormat(ScsbConstants.DEFAULT_DATE_FORMAT);
                Date fromDate = dateFormatter.parse(replaceRequest.getFromDate());
                Date toDate = dateFormatter.parse(replaceRequest.getToDate());
                if (ScsbConstants.REQUEST_STATUS_PENDING.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.getRequestsBasedOnDateRangeAndRequestStatusCode(fromDate, toDate, ScsbConstants.REQUEST_STATUS_PENDING);
                    resultMap = buildRequestInfoAndReplaceToLAS(requestItemEntities);
                } else if (ScsbConstants.REQUEST_STATUS_LAS_ITEM_STATUS_PENDING.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.getRequestsBasedOnDateRangeAndRequestStatusCode(fromDate, toDate, ScsbConstants.REQUEST_STATUS_LAS_ITEM_STATUS_PENDING);
                    resultMap = buildRequestInfoAndReplaceToLAS(requestItemEntities);
                } else if (ScsbConstants.REQUEST_STATUS_EXCEPTION.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.getRequestsBasedOnDateRangeAndRequestStatusCode(fromDate, toDate, ScsbConstants.REQUEST_STATUS_EXCEPTION);
                    resultMap = buildRequestInfoAndReplaceToSCSB(requestItemEntities);
                } else {
                    resultMap.put(ScsbCommonConstants.INVALID_REQUEST, ScsbConstants.REQUEST_STATUS_INVALID);
                }
            } else {
                resultMap.put(ScsbCommonConstants.INVALID_REQUEST, ScsbConstants.REQUEST_DATES_INVALID);
            }
        } else {
            resultMap.put(ScsbCommonConstants.INVALID_REQUEST, ScsbConstants.REQUEST_REPLACE_BY_TYPE_INVALID);
        }
        return resultMap;
    }

    /**
     * Builds request information and replaces them to LAS queue.
     * @param requestItemEntities
     * @return
     */
    private Map<String, String> buildRequestInfoAndReplaceToLAS(List<RequestItemEntity> requestItemEntities) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put(ScsbCommonConstants.TOTAL_REQUESTS_FOUND, String.valueOf(requestItemEntities.size()));
        if (CollectionUtils.isNotEmpty(requestItemEntities)) {
            String message;
            for (RequestItemEntity requestItemEntity : requestItemEntities) {
                String requestTypeCode = requestItemEntity.getRequestTypeEntity().getRequestTypeCode();
                if (ScsbCommonConstants.RETRIEVAL.equalsIgnoreCase(requestTypeCode) || ScsbConstants.EDD_REQUEST.equalsIgnoreCase(requestTypeCode)) {
                    message = gfaLasService.buildRequestInfoAndReplaceToLAS(requestItemEntity);
                } else {
                    message = ScsbConstants.IGNORE_REQUEST_TYPE_NOT_VALID + requestTypeCode;
                }
                resultMap.put(String.valueOf(requestItemEntity.getId()), message);
            }
        } else {
            resultMap.put(ScsbCommonConstants.INVALID_REQUEST, ScsbConstants.NO_REQUESTS_FOUND);
        }
        return resultMap;
    }

    /**
     * Builds request information and replaces them to SCSB queue.
     * @param requestItemEntities
     * @return
     */
    private Map<String, String> buildRequestInfoAndReplaceToSCSB(List<RequestItemEntity> requestItemEntities) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put(ScsbCommonConstants.TOTAL_REQUESTS_FOUND, String.valueOf(requestItemEntities.size()));
        if (CollectionUtils.isNotEmpty(requestItemEntities)) {
            String message;
            for (RequestItemEntity requestItemEntity : requestItemEntities) {
                String requestTypeCode = requestItemEntity.getRequestTypeEntity().getRequestTypeCode();
                if (ScsbCommonConstants.RETRIEVAL.equalsIgnoreCase(requestTypeCode)) {
                    message = buildRetrieveRequestInfoAndReplaceToSCSB(requestItemEntity);
                } else if (ScsbConstants.EDD_REQUEST.equalsIgnoreCase(requestTypeCode)) {
                    message = buildEddRequestInfoAndReplaceToSCSB(requestItemEntity);
                } else {
                    message = ScsbConstants.IGNORE_REQUEST_TYPE_NOT_VALID + requestTypeCode;
                }
                resultMap.put(String.valueOf(requestItemEntity.getId()), message);
            }
        } else {
            resultMap.put(ScsbCommonConstants.INVALID_REQUEST, ScsbConstants.NO_REQUESTS_FOUND);
        }
        return resultMap;
    }

    /**
     * Validates the request information and returns message.
     * @param itemRequestInformation
     * @return
     */
    private String validateItemRequest(ItemRequestInformation itemRequestInformation) {
        ResponseEntity responseEntity = requestParamaterValidatorService.validateItemRequestParameters(itemRequestInformation);
        if (responseEntity == null) {
            responseEntity = itemValidatorService.itemValidation(itemRequestInformation);
        }
        return responseEntity.getBody().toString();
    }

    /**
     * Builds retrieval request information and replaces them to SCSB queue.
     * @param requestItemEntity
     * @return
     */
    private String buildRetrieveRequestInfoAndReplaceToSCSB(RequestItemEntity requestItemEntity) {
        ItemRequestInformation itemRequestInformation = getItemRequestInformationByRequestEntity(requestItemEntity, requestItemEntity.getItemEntity());

        String validationMessage = validateItemRequest(itemRequestInformation);
        if (!ScsbCommonConstants.VALID_REQUEST.equals(validationMessage)) {
            return ScsbCommonConstants.FAILURE + " : " + validationMessage;
        }
        return setRequestItemEntity(itemRequestInformation, requestItemEntity);
    }

    public ItemRequestInformation getItemRequestInformationByRequestEntity(RequestItemEntity requestItemEntity, ItemEntity itemEntity) {
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setRequestId(requestItemEntity.getId());
        itemRequestInformation.setCustomerCode(requestItemEntity.getItemEntity().getCustomerCode());
        itemRequestInformation.setUsername(requestItemEntity.getCreatedBy());
        itemRequestInformation.setItemBarcodes(Collections.singletonList(itemEntity.getBarcode()));
        itemRequestInformation.setPatronBarcode(requestItemEntity.getPatronId());
        itemRequestInformation.setRequestingInstitution(requestItemEntity.getInstitutionEntity().getInstitutionCode());
        itemRequestInformation.setEmailAddress(securityUtil.getDecryptedValue(requestItemEntity.getEmailId()));
        itemRequestInformation.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
        itemRequestInformation.setRequestType(requestItemEntity.getRequestTypeEntity().getRequestTypeCode());
        itemRequestInformation.setDeliveryLocation(requestItemEntity.getStopCode());
        String imsLocationCode = commonUtil.getImsLocationCodeByItemBarcode(requestItemEntity.getItemEntity().getBarcode());
        itemRequestInformation.setImsLocationCode(imsLocationCode);

        String notes = requestItemEntity.getNotes();
        new BufferedReader(new StringReader(notes)).lines().forEach(line -> itemRequestServiceUtil.setEddInfoToScsbRequest(line, itemRequestInformation));
        return itemRequestInformation;
    }

    /**
     * Builds EDD request information and replaces them to SCSB queue.
     * @param requestItemEntity
     * @return
     */
    private String buildEddRequestInfoAndReplaceToSCSB(RequestItemEntity requestItemEntity) {
        ItemEntity itemEntity = requestItemEntity.getItemEntity();
        ItemRequestInformation itemRequestInformation = getItemRequestInformationByRequestEntity(requestItemEntity, itemEntity);

        SearchResultRow searchResultRow = searchRecords(itemEntity);
        itemRequestInformation.setTitleIdentifier(searchResultRow.getTitle());

        String validationMessage = validateItemRequest(itemRequestInformation);
        if (!ScsbCommonConstants.VALID_REQUEST.equals(validationMessage)) {
            return ScsbCommonConstants.FAILURE + ":" + validationMessage;
        }
        return setRequestItemEntity(itemRequestInformation, requestItemEntity);
    }

    private String setRequestItemEntity(ItemRequestInformation itemRequestInformation, RequestItemEntity requestItemEntity) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(itemRequestInformation);
            String itemStatus = gfaLasService.callGfaItemStatus(requestItemEntity.getItemEntity().getBarcode());
            if (commonUtil.checkIfImsItemStatusIsAvailableOrNotAvailable(requestItemEntity.getItemEntity().getImsLocationEntity().getImsLocationCode(), itemStatus, true)) {
                producerTemplate.sendBodyAndHeader(ScsbConstants.REQUEST_ITEM_QUEUE, json, ScsbCommonConstants.REQUEST_TYPE_QUEUE_HEADER, itemRequestInformation.getRequestType());
            } else if (StringUtils.isNotBlank(itemStatus)) {
                RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ScsbConstants.LAS_REFILE_REQUEST_PLACED);
                requestItemEntity.setRequestStatusEntity(requestStatusEntity);
                requestItemEntity.setRequestStatusId(requestStatusEntity.getId());
                requestItemDetailsRepository.save(requestItemEntity);
            }
        } catch (Exception exception) {
            logger.error(ScsbCommonConstants.REQUEST_EXCEPTION, exception);
            return ScsbCommonConstants.FAILURE + ":" + exception.getMessage();
        }
        return ScsbCommonConstants.SUCCESS + " : " + ScsbCommonConstants.REQUEST_MESSAGE_RECEVIED;
    }
}
