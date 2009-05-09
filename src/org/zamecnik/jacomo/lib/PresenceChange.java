package org.zamecnik.jacomo.lib;

import java.util.Date;

/**
 * Contact presence status change.
 * @see PresenceStatus
 * @author Bohumir Zamecnik
 */
public class PresenceChange {
    /** Contact identification in the database. */
    private int contactId;
    /** Date and time of change. */
    private Date time;
    /** New presence status. */
    private PresenceStatus status;
    /** New presence status text description. */
    private String statusDescription;

    /**
     * Presence change constructor.
     * @param time Date and time of change
     * @param status New presence status
     * @param contactId Contact identification in the database
     * @param statusDescription New presence status text description
     */
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

    /**
     * Simple presence change constructor. There is not contact id and text
     * description of status.
     * @param time Date and time of change
     * @param status New presence status
     */
    public PresenceChange(Date time, PresenceStatus status) {
        this(time, status, 0, "");
    }

    /**
     * Get contact id.
     * @return the contactId
     */
    public int getContactId() {
        return contactId;
    }

    /**
     * Set contact id.
     * @param contactId the contactId to set
     */
    private void setContactId(int contactId) {
        this.contactId = contactId;
    }

    /**
     * Get time of change.
     * @return the time
     */
    public Date getTime() {
        return time;
    }

    /**
     * Set time of change.
     * @param time the time to set
     */
    private void setTime(Date time) {
        this.time = time;
    }

    /**
     * Get presence status.
     * @return the status
     */
    public PresenceStatus getStatus() {
        return status;
    }

    /**
     * Set presence status.
     * @param status the status to set
     */
    private void setStatus(PresenceStatus status) {
        this.status = status;
    }

    /**
     * Get presence status description.
     * @return the statusDescription
     */
    public String getStatusDescription() {
        return statusDescription;
    }

    /**
     * Get presence status description.
     * @param statusDescription the statusDescription to set
     */
    private void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }
}
