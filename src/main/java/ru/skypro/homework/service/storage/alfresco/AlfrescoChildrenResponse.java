package ru.skypro.homework.service.storage.alfresco;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlfrescoChildrenResponse {

    private AlfrescoChildrenList list;

    public AlfrescoChildrenList getList() {
        return list;
    }

    public void setList(AlfrescoChildrenList list) {
        this.list = list;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AlfrescoChildrenList {

        private Pagination pagination;
        private List<Entry> entries;

        public Pagination getPagination() {
            return pagination;
        }

        public void setPagination(Pagination pagination) {
            this.pagination = pagination;
        }

        public List<Entry> getEntries() {
            return entries;
        }

        public void setEntries(List<Entry> entries) {
            this.entries = entries;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pagination {

        private int count;
        private boolean hasMoreItems;
        private int totalItems;
        private int skipCount;
        private int maxItems;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public boolean isHasMoreItems() {
            return hasMoreItems;
        }

        public void setHasMoreItems(boolean hasMoreItems) {
            this.hasMoreItems = hasMoreItems;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public void setTotalItems(int totalItems) {
            this.totalItems = totalItems;
        }

        public int getSkipCount() {
            return skipCount;
        }

        public void setSkipCount(int skipCount) {
            this.skipCount = skipCount;
        }

        public int getMaxItems() {
            return maxItems;
        }

        public void setMaxItems(int maxItems) {
            this.maxItems = maxItems;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {

        private AlfrescoChildrenEntry entry;

        public AlfrescoChildrenEntry getEntry() {
            return entry;
        }

        public void setEntry(AlfrescoChildrenEntry entry) {
            this.entry = entry;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AlfrescoChildrenEntry {

        private String id;
        private String name;
        private boolean isFolder;
        private OffsetDateTime createdAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isFolder() {
            return isFolder;
        }

        public void setFolder(boolean folder) {
            isFolder = folder;
        }

        public OffsetDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}