package com.groupstp.datasupplier.core.bean.datasupplier;

import com.groupstp.datasupplier.core.bean.DataSupplierWorker;
import com.groupstp.datasupplier.core.bean.datasupplier.provider.DataProviderDelegate;
import com.groupstp.datasupplier.core.config.DataSupplierConfig;
import com.groupstp.datasupplier.data.AddressData;
import com.haulmont.cuba.core.global.AppBeans;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generic data supplier logic worker bean
 *
 * @author adiatullin
 */
@Component(DataSupplierWorker.NAME)
public class DataSupplierWorkerBean implements DataSupplierWorker {
    private static final Logger log = LoggerFactory.getLogger(DataSupplierWorkerBean.class);

    @Inject
    protected DataSupplierConfig config;

    @Override
    public String getFormattedAddress(String rawAddress) {
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            return rawAddress;
        }
        String result = prepareAddress(getFormattedAddressDetails(rawAddress));
        return StringUtils.isBlank(result) ? rawAddress : result;
    }

    @Override
    public AddressData getFormattedAddressDetails(String rawAddress) {
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            return null;
        }
        if (!StringUtils.isBlank(rawAddress)) {
            List<DataProviderDelegate> list = getDelegates();
            if (!CollectionUtils.isEmpty(list)) {
                for (DataProviderDelegate delegate : list) {
                    try {
                        AddressData result = delegate.getFormattedAddressDetails(rawAddress);
                        if (result != null && !StringUtils.isBlank(result.getAddress())) {
                            return result;
                        }
                    } catch (Exception e) {
                        log.error(String.format("Failed to clean address from data delegate '%s'. Reason: %s", delegate.getClass(), e.getMessage()));
                    }
                }
                log.warn(String.format("Current data delegates are not support to clean up address '%s'", rawAddress));
            } else {
                log.warn("Data provider delegates are not registered in system");
            }
        } else {
            log.warn("Tried to clean address from empty data");
        }
        return null;
    }

    @Nullable
    @Override
    public AddressData getExtendedSuggestionAddressDetails(AddressData selected) {
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            return null;
        }
        if (selected != null) {
            List<DataProviderDelegate> list = getDelegates();
            if (!CollectionUtils.isEmpty(list)) {
                for (DataProviderDelegate delegate : list) {
                    try {
                        AddressData result = delegate.getExtendedSuggestionAddressDetails(selected);
                        if (result != null) {
                            return result;
                        }
                    } catch (Exception e) {
                        log.error(String.format("Failed to prepare more detailed suggestion address from data delegate '%s'. Reason: %s", delegate.getClass(), e.getMessage()));
                    }
                }
                log.warn("Current data delegates are not support preparing more detailed suggestion address");
            } else {
                log.warn("Data provider delegates are not registered in system");
            }
        }
        return null;
    }

    @Override
    public List<String> getSuggestionAddresses(String rawAddress, int count) {
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            return Collections.emptyList();
        }
        List<AddressData> addresses = getSuggestionAddressesDetails(rawAddress, count);
        if (!CollectionUtils.isEmpty(addresses)) {
            return addresses.stream()
                    .map(this::prepareAddress)
                    .filter(e -> !StringUtils.isBlank(e))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<AddressData> getSuggestionAddressesDetails(String rawAddress, int count) {
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            return Collections.emptyList();
        }
        if (!StringUtils.isBlank(rawAddress)) {
            List<DataProviderDelegate> list = getDelegates();
            if (!CollectionUtils.isEmpty(list)) {
                for (DataProviderDelegate delegate : list) {
                    try {
                        List<AddressData> result = delegate.getSuggestionAddressesDetails(rawAddress, count);
                        if (!CollectionUtils.isEmpty(result)) {
                            return result;
                        }
                    } catch (Exception e) {
                        log.error(String.format("Failed to receive suggestion address from data delegate '%s'. Reason: %s", delegate.getClass(), e.getMessage()));
                    }
                }
                log.warn(String.format("Current data delegates are not support to get suggestion addresses from value '%s'", rawAddress));
            } else {
                log.warn("Data provider delegates are not registered in system");
            }
        } else {
            log.warn("Tried to get suggestion address from empty data");
        }
        return Collections.emptyList();
    }

    @Override
    public List<AddressData> getSuggestionAddressesDetails(double latitude, double longitude, int count) {
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            return Collections.emptyList();
        }
        List<DataProviderDelegate> list = getDelegates();
        if (!CollectionUtils.isEmpty(list)) {
            for (DataProviderDelegate delegate : list) {
                try {
                    List<AddressData> result = delegate.getSuggestionAddressesDetails(latitude, longitude, count);
                    if (!CollectionUtils.isEmpty(result)) {
                        return result;
                    }
                } catch (Exception e) {
                    log.error(String.format("Failed to receive suggestion address from geo coordinates by data delegate '%s'. Reason: %s", delegate.getClass(), e.getMessage()));
                }
            }
            log.warn(String.format("Current data delegates are not support to get suggestion addresses from geo coordinates '%f':'%f'", latitude, longitude));
        } else {
            log.warn("Data provider delegates are not registered in system");
        }
        return Collections.emptyList();
    }

    @Nullable
    protected String prepareAddress(@Nullable AddressData address) {
        String result = null;
        if (address != null && !StringUtils.isBlank(address.getAddress())) {
            result = address.getAddress();
            if (!StringUtils.isBlank(address.getPostalCode())) {
                result = address.getPostalCode() + ", " + result;
            }
        }
        return result;
    }

    @Nullable
    protected List<DataProviderDelegate> getDelegates() {
        Map<String, DataProviderDelegate> items = AppBeans.getAll(DataProviderDelegate.class);
        if (items != null && items.size() > 0) {
            List<DataProviderDelegate> list = new ArrayList<>(items.size());
            list.addAll(items.values());
            list.sort(Comparator.comparingInt(Ordered::getOrder));
            return list;
        }
        return null;
    }
}
