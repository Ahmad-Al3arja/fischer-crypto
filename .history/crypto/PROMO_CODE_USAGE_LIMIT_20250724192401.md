# Promo Code Usage Limit Management

## Overview
The system now supports a default usage limit of 100 for all promo codes, with admin control to modify this default value.

## Features

### Default Usage Limit
- All new promo codes automatically get a usage limit of 100 if no limit is specified
- Existing promo codes with null or zero usage limits are updated to 100
- Admins can change the default limit through admin settings

### Admin Control
- Admins can view the current default usage limit
- Admins can update the default usage limit for future promo codes
- Changes take effect immediately for new promo codes

## API Endpoints

### Get Default Usage Limit
```
GET /api/admin/settings/default-usage-limit
```
**Response:**
```json
{
  "usageLimit": 100
}
```

### Update Default Usage Limit
```
POST /api/admin/settings/default-usage-limit
```
**Request Body:**
```json
{
  "usageLimit": 150
}
```
**Response:**
```json
{
  "message": "Default usage limit updated successfully"
}
```

## Usage Examples

### Creating Promo Code with Default Limit
```json
POST /api/admin/promo-codes
{
  "code": "WELCOME2024",
  "bonusValue": 10.00
  // usageLimit not specified - will use default (100)
}
```

### Creating Promo Code with Custom Limit
```json
POST /api/admin/promo-codes
{
  "code": "SPECIAL50",
  "bonusValue": 25.00,
  "usageLimit": 50
}
```

## Database Changes
- New migration `V3__Add_default_usage_limit.sql` adds admin setting
- Existing promo codes updated to have usage limit of 100
- Admin settings table includes `default_promo_usage_limit` setting

## Implementation Details
- Default value is stored in `admin_settings` table
- PromoCode entity has default value of 100
- PromoCodeService checks admin settings when creating codes
- AdminSettingsService provides methods to get/update default limit 