package lib;

import java.util.Date;

/**
 *
 * @author Bohouš
 */
public class PresenceChange {
    private int contactId;
    private Date time;
    private PresenceStatus status;
    private String statusDescription;

    public PresenceChange(
            Date time,
            PresenceStatus status,
            int contactId,
            String statusDescription)
    {
        setContactId(contactId);
        setTime(time);
        setStatus(status);
        setStatusDescription(statusDescription);
    }

    public PresenceChange(Date time, PresenceStatus status) {
        this(time, status, 0, "");
    }

    /**
     * @return the contactId
     */
    public int getContactId() {
        return contactId;
    }

    /**
     * @param contactId the contactId to set
     */
    private void setContactId(int contactId) {
        this.contactId = contactId;
    }

    /**
     * @return the time
     */
    public Date getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    private void setTime(Date time) {
        this.time = time;
    }

    /**
     * @return the status
     */
    public PresenceStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    private void setStatus(PresenceStatus status) {
        this.status = status;
    }

    /**
     * @return the statusDescription
     */
    public String getStatusDescription() {
        return statusDescription;
    }

    /**
     * @param statusDescription the statusDescription to set
     */
    private void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }
}
