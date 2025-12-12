# UI Integration Plan for Customization Features

## Current Status

### ✅ Backend Complete
- DocumentTemplateService with full CRUD
- CustomWorkflowService with workflow engine
- WebhookService with HMAC security
- 50+ system configurations
- REST Controllers for all features
- Comprehensive documentation

### ⏳ Frontend Partially Complete
- ✅ Basic customization page exists (`/customization/page.tsx`)
- ✅ Theme customization UI working
- ✅ Custom Fields UI working
- ✅ Basic Configuration UI working
- ✅ API client has theme, custom field, and basic config methods
- ⏳ Need to add Document Templates UI
- ⏳ Need to add Workflows UI
- ⏳ Need to add Webhooks UI
- ⏳ API client extended with new methods (just completed)

## What Needs to Be Done

### 1. Enhance Customization Page

Add 3 new tabs to `/app/(app)/customization/page.tsx`:

#### Tab 4: Document Templates 📄
**Features:**
- List all templates by type (Invoice, Receipt, Quotation, etc.)
- Create/Edit template with:
  - Template name
  - Template type selector
  - Template format (HTML, PDF, Excel, CSV)
  - Rich HTML editor for template content
  - CSS editor
  - Header/Footer editors
  - Page settings (size, orientation, margins)
  - Display options (logo, company details, tax, terms)
- Set default template
- Preview template with sample data
- Delete template

**UI Components Needed:**
```typescript
- TemplateList: Display all templates
- TemplateEditor: Rich editor for template content
- TemplatePreview: Live preview with sample data
- PageSettings: Configure page size, margins, etc.
```

#### Tab 5: Workflows ⚙️
**Features:**
- List all workflows
- Filter by trigger event
- Create/Edit workflow with:
  - Workflow name and description
  - Trigger event selector (INVOICE_CREATED, INVOICE_PAID, LOW_STOCK, etc.)
  - Condition builder:
    - Add multiple conditions
    - Field selector
    - Operator selector (EQUALS, GREATER_THAN, CONTAINS, etc.)
    - Value input
  - Action builder:
    - Add multiple actions
    - Action type selector (SEND_EMAIL, SEND_SMS, CREATE_TASK, etc.)
    - Action configuration (recipient, message, etc.)
  - Execution order
  - Active/Inactive toggle
- View execution statistics (count, failures, last executed)
- Delete workflow

**UI Components Needed:**
```typescript
- WorkflowList: Display all workflows with stats
- WorkflowBuilder: Visual workflow builder
- ConditionBuilder: Add/edit conditions
- ActionBuilder: Add/edit actions
- WorkflowStats: Show execution metrics
```

#### Tab 6: Webhooks 🔗
**Features:**
- List all webhooks
- Filter by event type
- Create/Edit webhook with:
  - Webhook name and description
  - Event type selector
  - Target URL
  - HTTP method (POST, PUT, PATCH)
  - Custom headers (key-value pairs)
  - Payload template with merge fields
  - Secret key for HMAC
  - Retry settings (count, interval, timeout)
  - Active/Inactive toggle
- Test webhook connection
- View success/failure statistics
- Delete webhook

**UI Components Needed:**
```typescript
- WebhookList: Display all webhooks with stats
- WebhookForm: Create/edit webhook
- HeadersEditor: Add custom headers
- PayloadEditor: Edit JSON payload template
- WebhookTester: Test webhook connection
- WebhookStats: Show success/failure metrics
```

### 2. Update Existing customizationApi

✅ Already completed - added all new API methods for:
- Document Templates
- Workflows
- Webhooks

### 3. Integration with Existing Features

Make configurations actually work in the application:

#### In Invoice Creation (`/invoices/new`)
- Use `billing.invoice_prefix` from config
- Use `billing.payment_terms_days` for due date
- Use `billing.default_currency` for currency
- Apply custom fields for INVOICE entity
- Use default invoice template for printing

#### In Product Management (`/products`)
- Apply custom fields for PRODUCT entity
- Use `inventory.low_stock_threshold` for alerts
- Show low stock indicators based on config

#### In Customer Management (`/customers`)
- Apply custom fields for CUSTOMER entity
- Use `customer.loyalty_enabled` to show/hide loyalty
- Use `customer.segment_vip_threshold` for auto-segmentation

#### In Settings (`/settings`)
- Move some basic configs to settings page
- Add links to advanced customization page

### 4. Visual Enhancements

#### Template Preview
```typescript
// Show live preview of invoice template
<div className="preview-container">
  <iframe 
    srcDoc={renderTemplate(template, sampleData)}
    className="w-full h-full border"
  />
</div>
```

#### Workflow Visual Builder
```typescript
// Visual representation of workflow
<div className="workflow-diagram">
  <div className="trigger">📥 {workflow.triggerEvent}</div>
  <div className="conditions">
    {conditions.map(c => (
      <div>IF {c.field} {c.operator} {c.value}</div>
    ))}
  </div>
  <div className="actions">
    {actions.map(a => (
      <div>THEN {a.type}</div>
    ))}
  </div>
</div>
```

#### Webhook Stats Dashboard
```typescript
<div className="webhook-stats">
  <StatCard label="Success Rate" value={`${successRate}%`} />
  <StatCard label="Total Calls" value={webhook.successCount + webhook.failureCount} />
  <StatCard label="Last Triggered" value={formatDate(webhook.lastTriggeredAt)} />
</div>
```

## Implementation Steps

### Phase 1: Add New Tabs (Priority: HIGH)
1. Update tab navigation in customization page
2. Add "Document Templates" tab
3. Add "Workflows" tab
4. Add "Webhooks" tab

### Phase 2: Document Templates UI (Priority: HIGH)
1. Create template list component
2. Create template form with editors
3. Add page settings
4. Implement template preview
5. Connect to API

### Phase 3: Workflows UI (Priority: MEDIUM)
1. Create workflow list component
2. Create workflow form
3. Implement condition builder
4. Implement action builder
5. Add visual workflow diagram
6. Connect to API

### Phase 4: Webhooks UI (Priority: MEDIUM)
1. Create webhook list component
2. Create webhook form
3. Add headers and payload editors
4. Implement webhook tester
5. Add stats dashboard
6. Connect to API

### Phase 5: Integration (Priority: HIGH)
1. Update invoice creation to use configs
2. Update product pages to use configs
3. Update customer pages to use configs
4. Add custom fields to entity forms
5. Test end-to-end functionality

### Phase 6: Polish (Priority: LOW)
1. Add loading states
2. Add error handling
3. Add success messages
4. Add tooltips and help text
5. Mobile responsiveness
6. Add animations

## Code Examples

### Example: Template List Component

```typescript
const TemplateList = ({ templates, onEdit, onDelete, onSetDefault }) => {
  return (
    <div className="space-y-3">
      {templates.map(template => (
        <div key={template.id} className="p-4 border rounded-lg">
          <div className="flex justify-between items-start">
            <div>
              <h4 className="font-semibold">{template.templateName}</h4>
              <p className="text-sm text-gray-600">{template.templateType}</p>
              {template.isDefault && (
                <span className="text-xs bg-green-100 text-green-800 px-2 py-1 rounded">
                  Default
                </span>
              )}
            </div>
            <div className="flex space-x-2">
              <button onClick={() => onEdit(template)}>Edit</button>
              <button onClick={() => onSetDefault(template.id)}>Set Default</button>
              <button onClick={() => onDelete(template.id)}>Delete</button>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};
```

### Example: Workflow Builder Component

```typescript
const WorkflowBuilder = ({ workflow, onChange }) => {
  const [conditions, setConditions] = useState([]);
  const [actions, setActions] = useState([]);

  return (
    <div className="space-y-6">
      {/* Basic Info */}
      <input
        placeholder="Workflow Name"
        value={workflow.workflowName}
        onChange={(e) => onChange({ ...workflow, workflowName: e.target.value })}
      />

      {/* Trigger */}
      <select value={workflow.triggerEvent} onChange={...}>
        <option value="INVOICE_CREATED">Invoice Created</option>
        <option value="INVOICE_PAID">Invoice Paid</option>
        <option value="LOW_STOCK">Low Stock</option>
      </select>

      {/* Conditions */}
      <ConditionBuilder
        conditions={conditions}
        onChange={setConditions}
      />

      {/* Actions */}
      <ActionBuilder
        actions={actions}
        onChange={setActions}
      />
    </div>
  );
};
```

### Example: Webhook Tester

```typescript
const WebhookTester = ({ webhookId }) => {
  const [testing, setTesting] = useState(false);
  const [result, setResult] = useState(null);

  const handleTest = async () => {
    setTesting(true);
    try {
      const response = await customizationApi.testWebhook(webhookId);
      setResult(response.data);
    } catch (error) {
      setResult({ success: false, error: error.message });
    } finally {
      setTesting(false);
    }
  };

  return (
    <div>
      <button onClick={handleTest} disabled={testing}>
        {testing ? 'Testing...' : 'Test Connection'}
      </button>
      {result && (
        <div className={result.success ? 'text-green-600' : 'text-red-600'}>
          {result.success ? '✓ Success' : '✗ Failed: ' + result.error}
        </div>
      )}
    </div>
  );
};
```

## Files to Create/Modify

### New Files
- `/frontend/src/components/customization/TemplateEditor.tsx`
- `/frontend/src/components/customization/TemplateList.tsx`
- `/frontend/src/components/customization/WorkflowBuilder.tsx`
- `/frontend/src/components/customization/WorkflowList.tsx`
- `/frontend/src/components/customization/WebhookForm.tsx`
- `/frontend/src/components/customization/WebhookList.tsx`

### Modified Files
- ✅ `/frontend/src/lib/api/customization/customizationApi.ts` - Add new API methods (DONE)
- ⏳ `/frontend/src/app/(app)/customization/page.tsx` - Add new tabs and components
- ⏳ `/frontend/src/app/(app)/invoices/new/page.tsx` - Use configurations
- ⏳ `/frontend/src/app/(app)/products/page.tsx` - Use configurations
- ⏳ `/frontend/src/app/(app)/customers/page.tsx` - Use configurations

## Testing Checklist

- [ ] Can create/edit/delete document templates
- [ ] Template preview works with sample data
- [ ] Can set default template per type
- [ ] Can create/edit/delete workflows
- [ ] Workflow conditions evaluate correctly
- [ ] Workflow actions execute properly
- [ ] Can create/edit/delete webhooks
- [ ] Webhook test connection works
- [ ] HMAC signature is generated correctly
- [ ] Configurations are applied in invoices
- [ ] Configurations are applied in products
- [ ] Configurations are applied in customers
- [ ] Custom fields appear in entity forms
- [ ] Custom field values are saved/loaded correctly

## Estimated Effort

- **Document Templates UI**: 4-6 hours
- **Workflows UI**: 6-8 hours
- **Webhooks UI**: 4-6 hours
- **Integration with existing features**: 6-8 hours
- **Testing and bug fixes**: 4-6 hours
- **Total**: 24-34 hours

## Priority Order

1. **HIGH**: Extend customization page with new tabs
2. **HIGH**: Document Templates UI (most visible to users)
3. **HIGH**: Integration with invoice/product/customer pages
4. **MEDIUM**: Webhooks UI (for integrations)
5. **MEDIUM**: Workflows UI (advanced feature)
6. **LOW**: Polish and animations

## Success Criteria

✅ Users can create custom document templates via UI
✅ Users can set up workflows via visual builder
✅ Users can configure webhooks for integrations
✅ Configurations actually affect application behavior
✅ Custom fields appear and work in entity forms
✅ Everything is tested and documented

## Next Immediate Action

**Start with Document Templates UI** as it's the most user-facing and impactful feature.
