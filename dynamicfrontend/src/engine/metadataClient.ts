/**
 * Metadata Client
 * 
 * Fetches and caches metadata from the backend.
 * Supports websocket updates for live metadata changes.
 */

import axios, { AxiosInstance } from 'axios';
import { create } from 'zustand';

// Metadata types
export interface FormMetadata {
  id: string;
  entity: string;
  title: string;
  layout: {
    type: string;
    areas?: Array<{ name: string; width: number }>;
  };
  fields: FieldMetadata[];
  actions: ActionMetadata[];
}

export interface FieldMetadata {
  name: string;
  component: string;
  label: string;
  required?: boolean;
  validation?: Record<string, any>;
  optionsSource?: {
    type: string;
    url?: string;
    data?: any[];
  };
  defaultValue?: any;
}

export interface ActionMetadata {
  id: string;
  label: string;
  type: 'submit' | 'action' | 'navigate';
  actionUrl?: string;
  target?: string;
  permission?: string;
}

export interface ListMetadata {
  id: string;
  entity: string;
  columns: ColumnMetadata[];
  pageSize: number;
  filters?: FieldMetadata[];
  rowActions?: ActionMetadata[];
}

export interface ColumnMetadata {
  field: string;
  label: string;
  sortable?: boolean;
  format?: string;
}

export interface PageMetadata {
  id: string;
  title: string;
  layout: {
    regions: string[];
    items: Array<{
      region: string;
      widget: string;
      props?: Record<string, any>;
    }>;
  };
  permissions?: string[];
}

export interface WorkflowMetadata {
  id: string;
  steps: WorkflowStepMetadata[];
}

export interface WorkflowStepMetadata {
  id: string;
  type: 'input' | 'server';
  action?: string;
  ui?: {
    component: string;
    props?: Record<string, any>;
  };
}

// Metadata cache store
interface MetadataStore {
  forms: Map<string, FormMetadata>;
  lists: Map<string, ListMetadata>;
  pages: Map<string, PageMetadata>;
  workflows: Map<string, WorkflowMetadata>;
  lastUpdated: Map<string, number>;
  
  setForm: (id: string, metadata: FormMetadata) => void;
  setList: (id: string, metadata: ListMetadata) => void;
  setPage: (id: string, metadata: PageMetadata) => void;
  setWorkflow: (id: string, metadata: WorkflowMetadata) => void;
  
  getForm: (id: string) => FormMetadata | undefined;
  getList: (id: string) => ListMetadata | undefined;
  getPage: (id: string) => PageMetadata | undefined;
  getWorkflow: (id: string) => WorkflowMetadata | undefined;
  
  invalidate: (type: string, id: string) => void;
  clear: () => void;
}

export const useMetadataStore = create<MetadataStore>((set, get) => ({
  forms: new Map(),
  lists: new Map(),
  pages: new Map(),
  workflows: new Map(),
  lastUpdated: new Map(),
  
  setForm: (id, metadata) => set((state) => {
    const newForms = new Map(state.forms);
    newForms.set(id, metadata);
    const newUpdated = new Map(state.lastUpdated);
    newUpdated.set(`form:${id}`, Date.now());
    return { forms: newForms, lastUpdated: newUpdated };
  }),
  
  setList: (id, metadata) => set((state) => {
    const newLists = new Map(state.lists);
    newLists.set(id, metadata);
    const newUpdated = new Map(state.lastUpdated);
    newUpdated.set(`list:${id}`, Date.now());
    return { lists: newLists, lastUpdated: newUpdated };
  }),
  
  setPage: (id, metadata) => set((state) => {
    const newPages = new Map(state.pages);
    newPages.set(id, metadata);
    const newUpdated = new Map(state.lastUpdated);
    newUpdated.set(`page:${id}`, Date.now());
    return { pages: newPages, lastUpdated: newUpdated };
  }),
  
  setWorkflow: (id, metadata) => set((state) => {
    const newWorkflows = new Map(state.workflows);
    newWorkflows.set(id, metadata);
    const newUpdated = new Map(state.lastUpdated);
    newUpdated.set(`workflow:${id}`, Date.now());
    return { workflows: newWorkflows, lastUpdated: newUpdated };
  }),
  
  getForm: (id) => get().forms.get(id),
  getList: (id) => get().lists.get(id),
  getPage: (id) => get().pages.get(id),
  getWorkflow: (id) => get().workflows.get(id),
  
  invalidate: (type, id) => set((state) => {
    const key = `${type}:${id}`;
    const newUpdated = new Map(state.lastUpdated);
    newUpdated.delete(key);
    
    const updates: Partial<MetadataStore> = { lastUpdated: newUpdated };
    
    if (type === 'form') {
      const newForms = new Map(state.forms);
      newForms.delete(id);
      updates.forms = newForms;
    } else if (type === 'list') {
      const newLists = new Map(state.lists);
      newLists.delete(id);
      updates.lists = newLists;
    } else if (type === 'page') {
      const newPages = new Map(state.pages);
      newPages.delete(id);
      updates.pages = newPages;
    } else if (type === 'workflow') {
      const newWorkflows = new Map(state.workflows);
      newWorkflows.delete(id);
      updates.workflows = newWorkflows;
    }
    
    return updates;
  }),
  
  clear: () => set({
    forms: new Map(),
    lists: new Map(),
    pages: new Map(),
    workflows: new Map(),
    lastUpdated: new Map(),
  }),
}));

// Metadata Client class
class MetadataClient {
  private api: AxiosInstance;
  private baseURL: string;
  
  constructor(baseURL: string = 'http://localhost:8081') {
    this.baseURL = baseURL;
    this.api = axios.create({
      baseURL,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }
  
  /**
   * Set tenant and user headers
   */
  setHeaders(tenantId: string, userId: string): void {
    this.api.defaults.headers.common['X-Tenant-Id'] = tenantId;
    this.api.defaults.headers.common['X-User-Id'] = userId;
  }
  
  /**
   * Fetch form metadata
   */
  async fetchForm(id: string): Promise<FormMetadata> {
    const store = useMetadataStore.getState();
    const cached = store.getForm(id);
    
    if (cached) {
      return cached;
    }
    
    const response = await this.api.get<FormMetadata>(`/api/metadata/form/${id}`);
    store.setForm(id, response.data);
    return response.data;
  }
  
  /**
   * Fetch list metadata
   */
  async fetchList(id: string): Promise<ListMetadata> {
    const store = useMetadataStore.getState();
    const cached = store.getList(id);
    
    if (cached) {
      return cached;
    }
    
    const response = await this.api.get<ListMetadata>(`/api/metadata/list/${id}`);
    store.setList(id, response.data);
    return response.data;
  }
  
  /**
   * Fetch page metadata
   */
  async fetchPage(id: string): Promise<PageMetadata> {
    const store = useMetadataStore.getState();
    const cached = store.getPage(id);
    
    if (cached) {
      return cached;
    }
    
    const response = await this.api.get<PageMetadata>(`/api/metadata/page/${id}`);
    store.setPage(id, response.data);
    return response.data;
  }
  
  /**
   * Fetch workflow metadata
   */
  async fetchWorkflow(id: string): Promise<WorkflowMetadata> {
    const store = useMetadataStore.getState();
    const cached = store.getWorkflow(id);
    
    if (cached) {
      return cached;
    }
    
    const response = await this.api.get<WorkflowMetadata>(`/api/metadata/workflow/${id}`);
    store.setWorkflow(id, response.data);
    return response.data;
  }
  
  /**
   * Invalidate cached metadata
   */
  invalidate(type: string, id: string): void {
    const store = useMetadataStore.getState();
    store.invalidate(type, id);
  }
  
  /**
   * Clear all cached metadata
   */
  clearCache(): void {
    const store = useMetadataStore.getState();
    store.clear();
  }
}

// Export singleton instance
export const metadataClient = new MetadataClient();

export default metadataClient;
