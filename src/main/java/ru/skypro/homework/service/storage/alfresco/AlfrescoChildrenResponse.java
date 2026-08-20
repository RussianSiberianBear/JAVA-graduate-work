package ru.skypro.homework.service.storage.alfresco;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO для парсинга ответа Alfresco API при запросе дочерних элементов папки.
 * <p>
 * Отражает структуру JSON‑ответа от Alfresco REST API (endpoint /alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/children).
 * Аннотация {@link JsonIgnoreProperties} позволяет игнорировать поля, не описанные в DTO,
 * что делает маппинг устойчивым к изменениям API или дополнительным метаданным.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlfrescoChildrenResponse {

    private AlfrescoChildrenList list;

    public AlfrescoChildrenList getList() {
        return list;
    }

    public void setList(AlfrescoChildrenList list) {
        this.list = list;
    }

    /**
     * Контейнер для списка дочерних элементов и пагинации.
     */
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

    /**
     * Данные пагинации ответа Alfresco.
     * <p>
     * Позволяет корректно обрабатывать постраничную навигацию: размер страницы,
     * смещение, общее количество элементов и признак наличия следующих страниц.
     * </p>
     */
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

    /**
     * Обертка над элементом списка (нужна из‑за структуры JSON Alfresco: { "entry": {...} }).
     */
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

    /**
     * Метаданные отдельного дочернего элемента (файла или папки) в Alfresco.
     * <p>
     * Содержит ключевые атрибуты: идентификатор, имя, признак папки, дату создания.
     * Поле {@code isFolder} критично для логики навигации по дереву папок,
     * а {@code id} — для последующих запросов (например, получения содержимого или загрузки файла).
     * </p>
     */
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

        /**
         * Сеттер с корректным именем (в Alfresco поле называется isFolder, поэтому используем setFolder).
         */
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
