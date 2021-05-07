package org.recap.request;


import org.recap.ScsbConstants;
import org.recap.ScsbCommonConstants;
import org.recap.controller.ItemController;
import org.recap.model.jpa.ItemRequestInformation;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hemalathas on 3/11/16.
 */
@Component
public class RequestParamaterValidatorService {

    private static final Logger logger = LoggerFactory.getLogger(RequestParamaterValidatorService.class);

    /**
     * The Scsb solr client url.
     */
    @Value("${scsb.solr.doc.url}")
    String scsbSolrClientUrl;

    /**
     * The Item controller.
     */
    @Autowired
    ItemController itemController;

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    /**
     * Validate item request parameters response entity.
     *
     * @param itemRequestInformation the item request information
     * @return the response entity
     */
    public ResponseEntity validateItemRequestParameters(ItemRequestInformation itemRequestInformation) {
        ResponseEntity responseEntity = null;
        Map<Integer, String> errorMessageMap = new HashMap<>();
        Integer errorCount = 1;
        if (CollectionUtils.isEmpty(itemRequestInformation.getItemBarcodes())) {
            errorMessageMap.put(errorCount, ScsbConstants.ITEM_BARCODE_IS_REQUIRED);
            errorCount++;
        }
        if (StringUtils.isEmpty(itemRequestInformation.getRequestingInstitution()) || !institutionDetailsRepository.existsByInstitutionCode(itemRequestInformation.getRequestingInstitution())) {
            errorMessageMap.put(errorCount, MessageFormat.format(ScsbConstants.INVALID_REQUEST_INSTITUTION, String.join(",", institutionDetailsRepository.findAllInstitutionCodeExceptHTC())));
            errorCount++;
        }
        if (!validateEmailAddress(itemRequestInformation.getEmailAddress())) {
            errorMessageMap.put(errorCount, ScsbConstants.INVALID_EMAIL_ADDRESS);
            errorCount++;
        }

        if ((itemRequestInformation.getRequestType() == null || itemRequestInformation.getRequestType().trim().length() <= 0) || (!ScsbConstants.getRequestTypeList().contains(itemRequestInformation.getRequestType()))) {
            errorMessageMap.put(errorCount, ScsbConstants.INVALID_REQUEST_TYPE);
            errorCount++;
        } else {
            if (itemRequestInformation.getRequestType().equalsIgnoreCase(ScsbConstants.EDD_REQUEST)) {
                if (!CollectionUtils.isEmpty(itemRequestInformation.getItemBarcodes())) {
                    if (itemController.splitStringAndGetList(itemRequestInformation.getItemBarcodes().toString()).size() > 1) {
                        errorMessageMap.put(errorCount, ScsbConstants.MULTIPLE_ITEMS_NOT_ALLOWED_FOR_EDD);
                        errorCount++;
                    }
                } else {
                    errorMessageMap.put(errorCount, ScsbConstants.ITEM_BARCODE_IS_REQUIRED);
                    errorCount++;
                }
                if (StringUtils.isEmpty(itemRequestInformation.getChapterTitle())) {
                    errorMessageMap.put(errorCount, ScsbConstants.CHAPTER_TITLE_IS_REQUIRED);
                    errorCount++;
                }
                if (itemRequestInformation.getStartPage() == null || itemRequestInformation.getEndPage() == null) {
                    errorMessageMap.put(errorCount, ScsbConstants.START_PAGE_AND_END_PAGE_REQUIRED);
                    errorCount++;
                }
            } else if ((itemRequestInformation.getRequestType().equalsIgnoreCase(ScsbCommonConstants.REQUEST_TYPE_RECALL) || itemRequestInformation.getRequestType().equalsIgnoreCase(ScsbCommonConstants.RETRIEVAL)) &&
                 (StringUtils.isEmpty(itemRequestInformation.getDeliveryLocation()))) {
                    errorMessageMap.put(errorCount, ScsbConstants.DELIVERY_LOCATION_REQUIRED);
                    errorCount++;
                }
            }
       
        if (errorMessageMap.size() > 0) {
            return new ResponseEntity(buildErrorMessage(errorMessageMap), getHttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        return responseEntity;
    }

    private boolean validateEmailAddress(String toEmailAddress) {
        boolean bSuccess = false;
        try {
            if (!StringUtils.isEmpty(toEmailAddress)) {
                String regex = ScsbCommonConstants.REGEX_FOR_EMAIL_ADDRESS;
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(toEmailAddress);
                bSuccess = matcher.matches();
            } else {
                bSuccess = true;
            }
        } catch (Exception e) {
            logger.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return bSuccess;
    }

    /**
     * Gets http headers.
     *
     * @return the http headers
     */
    public HttpHeaders getHttpHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(ScsbCommonConstants.RESPONSE_DATE, new Date().toString());
        return responseHeaders;
    }

    private String buildErrorMessage(Map<Integer, String> erroMessageMap) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        erroMessageMap.forEach((key, value) -> errorMessageBuilder.append(value).append("\n"));
        return errorMessageBuilder.toString();
    }
}
