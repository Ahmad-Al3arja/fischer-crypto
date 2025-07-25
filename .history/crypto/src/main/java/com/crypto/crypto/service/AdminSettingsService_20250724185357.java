package com.crypto.crypto.service;

import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.entity.AdminSettings;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.UserStatus;
import com.crypto.crypto.repository.AdminSettingsRepository;
import com.crypto.crypto.repository.UserRepository;
import com.crypto.crypto.repository.DepositRepository;
import com.crypto.crypto.repository.WithdrawalRepository;
import com.crypto.crypto.repository.WalletChangeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AdminSettingsService {

    @Autowired
    private AdminSettingsRepository adminSettingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    @Autowired
    private WalletChangeRequestRepository walletChangeRequestRepository;

    @Value("${app.platform.usdt-wallet:TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE}")
    private String usdtWalletAddress;

    public AdminDTOs.AdminSettingsResponse getAdminSettings() {
        AdminDTOs.AdminSettingsResponse response = new AdminDTOs.AdminSettingsResponse();
        
        response.setMaintenanceMode(getMaintenanceMode());
        response.setAboutContent(getAboutContent());
        response.setUsdtWalletAddress(usdtWalletAddress);
        
        return response;
    }

    public void setMaintenanceMode(boolean enabled) {
        setSetting("maintenance_mode", String.valueOf(enabled), "Platform maintenance mode");
    }

    public void updateAboutContent(String content) {
        setSetting("about_content", content, "About platform content");
    }

    public void updateSystemSetting(String category, String keyName, String value) {
        String settingKey = category != null ? category + "_" + keyName : keyName;
        setSetting(settingKey, value, "System setting: " + settingKey);
    }

    public AdminDTOs.DashboardResponse getAdminDashboard() {
        AdminDTOs.DashboardResponse response = new AdminDTOs.DashboardResponse();

        // Get dashboard statistics
        AdminDTOs.DashboardResponse.DashboardStats stats = getDashboardStats();
        response.setStats(stats);

        // Get recent activities
        List<AdminDTOs.DashboardResponse.RecentActivity> recentActivities = getRecentActivities();
        response.setRecentActivities(recentActivities);

        // Get pending actions
        List<AdminDTOs.DashboardResponse.PendingAction> pendingActions = getPendingActions();
        response.setPendingActions(pendingActions);

        // Get system health
        AdminDTOs.DashboardResponse.SystemHealth systemHealth = getSystemHealth();
        response.setSystemHealth(systemHealth);

        return response;
    }

    public AdminDTOs.AnalyticsResponse getAnalytics(String period, String type) {
        AdminDTOs.AnalyticsResponse response = new AdminDTOs.AnalyticsResponse();
        response.setPeriod(period != null ? period : "30days");
        response.setType(type != null ? type : "overview");

        // Generate sample analytics data points
        List<AdminDTOs.AnalyticsResponse.AnalyticsDataPoint> dataPoints = generateAnalyticsDataPoints(period, type);
        response.setDataPoints(dataPoints);

        // Calculate summary
        AdminDTOs.AnalyticsResponse.AnalyticsSummary summary = calculateAnalyticsSummary(dataPoints);
        response.setSummary(summary);

        return response;
    }

    private boolean getMaintenanceMode() {
        String value = getSettingValue("maintenance_mode");
        return value != null && Boolean.parseBoolean(value);
    }

    private String getAboutContent() {
        String value = getSettingValue("about_content");
        return value != null ? value : "Welcome to our investment platform!";
    }

    private String getSettingValue(String key) {
        return adminSettingsRepository.findBySettingKey(key)
                .map(AdminSettings::getSettingValue)
                .orElse(null);
    }

    private void setSetting(String key, String value, String description) {
        AdminSettings setting = adminSettingsRepository.findBySettingKey(key)
                .orElse(new AdminSettings());
        
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setDescription(description);
        
        // Set timestamps for new entities
        if (setting.getId() == null) {
            setting.setCreatedAt(LocalDateTime.now());
        }
        setting.setUpdatedAt(LocalDateTime.now());
        
        adminSettingsRepository.save(setting);
    }

    private AdminDTOs.DashboardResponse.DashboardStats getDashboardStats() {
        AdminDTOs.DashboardResponse.DashboardStats stats = new AdminDTOs.DashboardResponse.DashboardStats();

        // Count users
        List<User> allUsers = userRepository.findByRole(com.crypto.crypto.entity.Role.USER);
        stats.setTotalUsers(allUsers.size());
        
        int activeUsers = (int) allUsers.stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .count();
        stats.setActiveUsers(activeUsers);

        // Calculate total balances
        BigDecimal totalBalance = allUsers.stream()
                .map(User::getTotalBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalBalance(totalBalance);

        // Count deposits and withdrawals
        long pendingDeposits = depositRepository.findByStatus(
                com.crypto.crypto.entity.DepositStatus.PENDING).size();
        stats.setPendingDeposits((int) pendingDeposits);

        long pendingWithdrawals = withdrawalRepository.findByStatus(
                com.crypto.crypto.entity.WithdrawalStatus.PENDING).size();
        stats.setPendingWithdrawals((int) pendingWithdrawals);

        // Count pending wallet changes
        long pendingWalletChanges = walletChangeRequestRepository.findByStatusOrderByRequestedAtAsc(
                com.crypto.crypto.entity.WalletChangeStatus.PENDING).size();
        stats.setPendingWalletChanges((int) pendingWalletChanges);

        // Calculate total deposits and withdrawals
        BigDecimal totalDeposits = depositRepository.findByStatus(
                com.crypto.crypto.entity.DepositStatus.APPROVED).stream()
                .map(com.crypto.crypto.entity.Deposit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalDeposits(totalDeposits);

        BigDecimal totalWithdrawals = withdrawalRepository.findByStatus(
                com.crypto.crypto.entity.WithdrawalStatus.APPROVED).stream()
                .map(com.crypto.crypto.entity.Withdrawal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalWithdrawals(totalWithdrawals);

        return stats;
    }

    private List<AdminDTOs.DashboardResponse.RecentActivity> getRecentActivities() {
        List<AdminDTOs.DashboardResponse.RecentActivity> activities = new ArrayList<>();

        // Get recent deposits
        depositRepository.findByStatus(com.crypto.crypto.entity.DepositStatus.PENDING)
                .stream()
                .limit(5)
                .forEach(deposit -> {
                    AdminDTOs.DashboardResponse.RecentActivity activity = 
                            new AdminDTOs.DashboardResponse.RecentActivity();
                    activity.setType("DEPOSIT");
                    activity.setDescription("New deposit request");
                    activity.setUsername(deposit.getUser().getDisplayUsername());
                    activity.setAmount(deposit.getAmount());
                    activity.setTimestamp(deposit.getCreatedAt());
                    activities.add(activity);
                });

        // Get recent withdrawals
        withdrawalRepository.findByStatus(com.crypto.crypto.entity.WithdrawalStatus.PENDING)
                .stream()
                .limit(5)
                .forEach(withdrawal -> {
                    AdminDTOs.DashboardResponse.RecentActivity activity = 
                            new AdminDTOs.DashboardResponse.RecentActivity();
                    activity.setType("WITHDRAWAL");
                    activity.setDescription("New withdrawal request");
                    activity.setUsername(withdrawal.getUser().getDisplayUsername());
                    activity.setAmount(withdrawal.getAmount());
                    activity.setTimestamp(withdrawal.getCreatedAt());
                    activities.add(activity);
                });

        // Sort by timestamp descending
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        
        return activities.stream().limit(10).toList();
    }

    private List<AdminDTOs.DashboardResponse.PendingAction> getPendingActions() {
        List<AdminDTOs.DashboardResponse.PendingAction> actions = new ArrayList<>();

        // Pending deposits
        int pendingDeposits = depositRepository.findByStatus(
                com.crypto.crypto.entity.DepositStatus.PENDING).size();
        if (pendingDeposits > 0) {
            AdminDTOs.DashboardResponse.PendingAction action = 
                    new AdminDTOs.DashboardResponse.PendingAction();
            action.setType("DEPOSITS");
            action.setDescription("Deposits awaiting approval");
            action.setCount(pendingDeposits);
            action.setPriority("HIGH");
            action.setActionUrl("/api/admin/deposits?status=PENDING");
            actions.add(action);
        }

        // Pending withdrawals
        int pendingWithdrawals = withdrawalRepository.findByStatus(
                com.crypto.crypto.entity.WithdrawalStatus.PENDING).size();
        if (pendingWithdrawals > 0) {
            AdminDTOs.DashboardResponse.PendingAction action = 
                    new AdminDTOs.DashboardResponse.PendingAction();
            action.setType("WITHDRAWALS");
            action.setDescription("Withdrawals awaiting approval");
            action.setCount(pendingWithdrawals);
            action.setPriority("HIGH");
            action.setActionUrl("/api/admin/withdrawals?status=PENDING");
            actions.add(action);
        }

        // Pending wallet changes
        int pendingWalletChanges = walletChangeRequestRepository.findByStatusOrderByRequestedAtAsc(
                com.crypto.crypto.entity.WalletChangeStatus.PENDING).size();
        if (pendingWalletChanges > 0) {
            AdminDTOs.DashboardResponse.PendingAction action = 
                    new AdminDTOs.DashboardResponse.PendingAction();
            action.setType("WALLET_CHANGES");
            action.setDescription("Wallet change requests awaiting review");
            action.setCount(pendingWalletChanges);
            action.setPriority("MEDIUM");
            action.setActionUrl("/api/admin/wallet-change-requests");
            actions.add(action);
        }

        return actions;
    }

    private AdminDTOs.DashboardResponse.SystemHealth getSystemHealth() {
        AdminDTOs.DashboardResponse.SystemHealth health = 
                new AdminDTOs.DashboardResponse.SystemHealth();

        health.setStatus("HEALTHY");
        health.setCpuUsage(65.2); // Mock data
        health.setMemoryUsage(78.5); // Mock data
        health.setDatabaseConnections(12L); // Mock data
        health.setLastBackup(LocalDateTime.now().minusHours(6)); // Mock data
        health.setMaintenanceMode(getMaintenanceMode());

        return health;
    }

    private List<AdminDTOs.AnalyticsResponse.AnalyticsDataPoint> generateAnalyticsDataPoints(String period, String type) {
        List<AdminDTOs.AnalyticsResponse.AnalyticsDataPoint> dataPoints = new ArrayList<>();

        // Generate mock analytics data based on period
        int days = switch (period != null ? period : "30days") {
            case "7days" -> 7;
            case "30days" -> 30;
            case "90days" -> 90;
            default -> 30;
        };

        for (int i = days - 1; i >= 0; i--) {
            AdminDTOs.AnalyticsResponse.AnalyticsDataPoint point = 
                    new AdminDTOs.AnalyticsResponse.AnalyticsDataPoint();
            
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            point.setDate(date);
            point.setLabel(date.format(DateTimeFormatter.ofPattern("MMM dd")));
            
            // Generate mock data based on type
            switch (type != null ? type : "overview") {
                case "users" -> {
                    point.setValue(BigDecimal.valueOf(Math.random() * 100));
                    point.setCount((int) (Math.random() * 50));
                }
                case "deposits" -> {
                    point.setValue(BigDecimal.valueOf(Math.random() * 10000));
                    point.setCount((int) (Math.random() * 20));
                }
                case "withdrawals" -> {
                    point.setValue(BigDecimal.valueOf(Math.random() * 5000));
                    point.setCount((int) (Math.random() * 15));
                }
                default -> {
                    point.setValue(BigDecimal.valueOf(Math.random() * 1000));
                    point.setCount((int) (Math.random() * 10));
                }
            }
            
            dataPoints.add(point);
        }

        return dataPoints;
    }

    private AdminDTOs.AnalyticsResponse.AnalyticsSummary calculateAnalyticsSummary(
            List<AdminDTOs.AnalyticsResponse.AnalyticsDataPoint> dataPoints) {
        
        AdminDTOs.AnalyticsResponse.AnalyticsSummary summary = 
                new AdminDTOs.AnalyticsResponse.AnalyticsSummary();

        if (dataPoints.isEmpty()) {
            summary.setTotalValue(BigDecimal.ZERO);
            summary.setTotalCount(0);
            summary.setAverageValue(BigDecimal.ZERO);
            summary.setGrowth(BigDecimal.ZERO);
            summary.setGrowthPercentage(0.0);
            return summary;
        }

        // Calculate totals
        BigDecimal totalValue = dataPoints.stream()
                .map(AdminDTOs.AnalyticsResponse.AnalyticsDataPoint::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalValue(totalValue);

        int totalCount = dataPoints.stream()
                .mapToInt(AdminDTOs.AnalyticsResponse.AnalyticsDataPoint::getCount)
                .sum();
        summary.setTotalCount(totalCount);

        // Calculate average
        BigDecimal averageValue = totalValue.divide(
                BigDecimal.valueOf(dataPoints.size()), 2, BigDecimal.ROUND_HALF_UP);
        summary.setAverageValue(averageValue);

        // Calculate growth (compare first and last periods)
        if (dataPoints.size() >= 2) {
            BigDecimal firstValue = dataPoints.get(0).getValue();
            BigDecimal lastValue = dataPoints.get(dataPoints.size() - 1).getValue();
            BigDecimal growth = lastValue.subtract(firstValue);
            summary.setGrowth(growth);

            if (firstValue.compareTo(BigDecimal.ZERO) != 0) {
                double growthPercentage = growth.divide(firstValue, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
                summary.setGrowthPercentage(growthPercentage);
            } else {
                summary.setGrowthPercentage(0.0);
            }
        } else {
            summary.setGrowth(BigDecimal.ZERO);
            summary.setGrowthPercentage(0.0);
        }

        return summary;
    }
}