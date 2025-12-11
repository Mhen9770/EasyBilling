/**
 * Plugin Loader
 * 
 * Dynamically loads and registers plugin components at runtime.
 */

import componentRegistry from './registry';

export interface PluginManifest {
  name: string;
  version: string;
  components?: Record<string, any>;
  widgets?: Record<string, any>;
  init?: () => void;
  destroy?: () => void;
}

class PluginLoader {
  private loadedPlugins: Map<string, PluginManifest> = new Map();

  /**
   * Load a plugin from a URL or module
   */
  async loadPlugin(url: string): Promise<void> {
    try {
      // Dynamic import for ES modules
      const module = await import(/* @vite-ignore */ url);
      const plugin: PluginManifest = module.default || module;

      if (!plugin.name) {
        throw new Error('Plugin must have a name');
      }

      // Check if already loaded
      if (this.loadedPlugins.has(plugin.name)) {
        console.warn(`Plugin ${plugin.name} is already loaded`);
        return;
      }

      // Register components
      if (plugin.components) {
        Object.entries(plugin.components).forEach(([name, component]) => {
          componentRegistry.register(name, component as any);
        });
      }

      // Register widgets
      if (plugin.widgets) {
        Object.entries(plugin.widgets).forEach(([name, widget]) => {
          componentRegistry.register(`Widget_${name}`, widget as any);
        });
      }

      // Initialize plugin
      if (plugin.init) {
        plugin.init();
      }

      this.loadedPlugins.set(plugin.name, plugin);
      console.log(`Plugin ${plugin.name} v${plugin.version} loaded successfully`);
    } catch (error) {
      console.error(`Failed to load plugin from ${url}:`, error);
      throw error;
    }
  }

  /**
   * Unload a plugin
   */
  unloadPlugin(pluginName: string): void {
    const plugin = this.loadedPlugins.get(pluginName);
    if (!plugin) {
      console.warn(`Plugin ${pluginName} is not loaded`);
      return;
    }

    // Call destroy hook
    if (plugin.destroy) {
      plugin.destroy();
    }

    // Unregister components
    if (plugin.components) {
      Object.keys(plugin.components).forEach((name) => {
        componentRegistry.unregister(name);
      });
    }

    // Unregister widgets
    if (plugin.widgets) {
      Object.keys(plugin.widgets).forEach((name) => {
        componentRegistry.unregister(`Widget_${name}`);
      });
    }

    this.loadedPlugins.delete(pluginName);
    console.log(`Plugin ${pluginName} unloaded`);
  }

  /**
   * Get loaded plugin
   */
  getPlugin(pluginName: string): PluginManifest | undefined {
    return this.loadedPlugins.get(pluginName);
  }

  /**
   * Get all loaded plugins
   */
  getAllPlugins(): string[] {
    return Array.from(this.loadedPlugins.keys());
  }

  /**
   * Check if plugin is loaded
   */
  isLoaded(pluginName: string): boolean {
    return this.loadedPlugins.has(pluginName);
  }
}

// Export singleton
export const pluginLoader = new PluginLoader();

export default pluginLoader;
