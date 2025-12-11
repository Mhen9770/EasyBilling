/**
 * Component Registry
 * 
 * Runtime registry for mapping component names to React components.
 * Supports lazy registration of plugin components.
 */

type ComponentConstructor = (props: any) => JSX.Element;

class ComponentRegistry {
  private components: Map<string, ComponentConstructor> = new Map();

  /**
   * Register a component with a given name
   */
  register(name: string, component: ComponentConstructor): void {
    if (this.components.has(name)) {
      console.warn(`Component "${name}" is already registered. Overwriting...`);
    }
    this.components.set(name, component);
    console.log(`Registered component: ${name}`);
  }

  /**
   * Register multiple components at once
   */
  registerBatch(components: Record<string, ComponentConstructor>): void {
    Object.entries(components).forEach(([name, component]) => {
      this.register(name, component);
    });
  }

  /**
   * Resolve a component by name
   */
  resolve(name: string): ComponentConstructor | undefined {
    const component = this.components.get(name);
    if (!component) {
      console.warn(`Component "${name}" not found in registry`);
    }
    return component;
  }

  /**
   * Check if a component is registered
   */
  has(name: string): boolean {
    return this.components.has(name);
  }

  /**
   * Get all registered component names
   */
  getAll(): string[] {
    return Array.from(this.components.keys());
  }

  /**
   * Unregister a component
   */
  unregister(name: string): boolean {
    return this.components.delete(name);
  }

  /**
   * Clear all registered components
   */
  clear(): void {
    this.components.clear();
  }
}

// Export singleton instance
export const componentRegistry = new ComponentRegistry();

export type { ComponentConstructor };
export default componentRegistry;
