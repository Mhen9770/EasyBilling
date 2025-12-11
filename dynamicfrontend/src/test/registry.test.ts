import { describe, it, expect } from 'vitest';
import componentRegistry from '../engine/registry';

describe('ComponentRegistry', () => {
  it('should register and resolve components', () => {
    const TestComponent = () => <div>Test</div>;
    
    componentRegistry.register('TestComponent', TestComponent);
    
    const resolved = componentRegistry.resolve('TestComponent');
    expect(resolved).toBe(TestComponent);
  });

  it('should return undefined for non-existent components', () => {
    const resolved = componentRegistry.resolve('NonExistentComponent');
    expect(resolved).toBeUndefined();
  });

  it('should check if component exists', () => {
    const TestComponent = () => <div>Test</div>;
    
    componentRegistry.register('ExistsTest', TestComponent);
    
    expect(componentRegistry.has('ExistsTest')).toBe(true);
    expect(componentRegistry.has('DoesNotExist')).toBe(false);
  });

  it('should unregister components', () => {
    const TestComponent = () => <div>Test</div>;
    
    componentRegistry.register('ToRemove', TestComponent);
    expect(componentRegistry.has('ToRemove')).toBe(true);
    
    componentRegistry.unregister('ToRemove');
    expect(componentRegistry.has('ToRemove')).toBe(false);
  });
});
