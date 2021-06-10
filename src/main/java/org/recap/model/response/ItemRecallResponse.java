package org.recap.model.response;

import lombok.Getter;
import lombok.Setter;
import org.recap.model.AbstractResponseItem;

/**
 * Created by sudhishk on 16/12/16.
 */
@Getter
@Setter
public class ItemRecallResponse extends AbstractResponseItem {

    private boolean available;
    private String transactionDate;
    private String institutionID;
    private String patronIdentifier;
    private String titleIdentifier;
    private String expirationDate;
    private String pickupLocation;
    private String queuePosition;
    private String bibId;
    private String isbn;
    private String lccn;
    private String jobId;

}
