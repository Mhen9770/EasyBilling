/**
 * Workflow UI Components
 * 
 * Components for rendering and executing workflow steps.
 */

import React, { useState } from 'react';
import { WorkflowMetadata, WorkflowStepMetadata } from './metadataClient';
import componentRegistry from './registry';
import axios from 'axios';

interface WorkflowRunnerProps {
  workflowMeta: WorkflowMetadata;
  initialData?: any;
  onComplete?: (result: any) => void;
  onError?: (error: any) => void;
}

export function WorkflowRunner({ workflowMeta, initialData = {}, onComplete, onError }: WorkflowRunnerProps) {
  const [currentStepIndex, setCurrentStepIndex] = useState(0);
  const [workflowData, setWorkflowData] = useState<any>(initialData);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const currentStep = workflowMeta.steps[currentStepIndex];
  const isLastStep = currentStepIndex === workflowMeta.steps.length - 1;

  const executeServerStep = async (step: WorkflowStepMetadata) => {
    setLoading(true);
    setError(null);

    try {
      const response = await axios.post(
        `http://localhost:8081/api/workflow/${workflowMeta.id}/step/${step.id}`,
        workflowData,
        {
          headers: {
            'X-Tenant-Id': 'default',
            'X-User-Id': 'admin',
            'Content-Type': 'application/json',
          },
        }
      );

      setWorkflowData({ ...workflowData, ...response.data });
      return response.data;
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Server step failed';
      setError(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleStepComplete = async (stepData: any) => {
    const updatedData = { ...workflowData, ...stepData };
    setWorkflowData(updatedData);

    if (currentStep.type === 'server') {
      try {
        await executeServerStep(currentStep);
      } catch (err) {
        if (onError) {
          onError(err);
        }
        return;
      }
    }

    if (isLastStep) {
      if (onComplete) {
        onComplete(updatedData);
      }
    } else {
      setCurrentStepIndex(currentStepIndex + 1);
      setError(null);
    }
  };

  const handlePrevious = () => {
    if (currentStepIndex > 0) {
      setCurrentStepIndex(currentStepIndex - 1);
      setError(null);
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      {/* Progress bar */}
      <div className="mb-6">
        <div className="flex items-center justify-between mb-2">
          <span className="text-sm font-medium text-gray-700">
            Step {currentStepIndex + 1} of {workflowMeta.steps.length}
          </span>
          <span className="text-sm text-gray-500">{currentStep.id}</span>
        </div>
        <div className="w-full bg-gray-200 rounded-full h-2">
          <div
            className="bg-blue-600 h-2 rounded-full transition-all duration-300"
            style={{ width: `${((currentStepIndex + 1) / workflowMeta.steps.length) * 100}%` }}
          />
        </div>
      </div>

      {/* Step indicator */}
      <div className="flex justify-between mb-8">
        {workflowMeta.steps.map((step, index) => (
          <div
            key={step.id}
            className={`flex-1 text-center ${
              index <= currentStepIndex ? 'text-blue-600' : 'text-gray-400'
            }`}
          >
            <div
              className={`w-8 h-8 mx-auto rounded-full flex items-center justify-center mb-2 ${
                index < currentStepIndex
                  ? 'bg-green-500 text-white'
                  : index === currentStepIndex
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-300 text-gray-600'
              }`}
            >
              {index < currentStepIndex ? '✓' : index + 1}
            </div>
            <div className="text-xs">{step.id}</div>
          </div>
        ))}
      </div>

      {/* Error display */}
      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-red-800 font-semibold">Error</p>
          <p className="text-red-600">{error}</p>
        </div>
      )}

      {/* Step content */}
      <div className="bg-white p-6 rounded-lg shadow-lg">
        {currentStep.type === 'input' && currentStep.ui ? (
          <WorkflowStepInput
            step={currentStep}
            data={workflowData}
            onComplete={handleStepComplete}
            loading={loading}
          />
        ) : currentStep.type === 'server' ? (
          <div className="text-center py-8">
            <div className="text-lg font-semibold mb-2">Processing: {currentStep.id}</div>
            {loading ? (
              <div className="flex justify-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
              </div>
            ) : (
              <button
                onClick={() => handleStepComplete({})}
                className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Execute
              </button>
            )}
          </div>
        ) : (
          <div className="text-center text-gray-500">
            Unknown step type: {currentStep.type}
          </div>
        )}

        {/* Navigation buttons */}
        <div className="flex justify-between mt-6 pt-4 border-t">
          <button
            onClick={handlePrevious}
            disabled={currentStepIndex === 0 || loading}
            className="px-6 py-2 bg-gray-600 text-white rounded hover:bg-gray-700 disabled:bg-gray-400"
          >
            Previous
          </button>
        </div>
      </div>
    </div>
  );
}

interface WorkflowStepInputProps {
  step: WorkflowStepMetadata;
  data: any;
  onComplete: (data: any) => void;
  loading: boolean;
}

function WorkflowStepInput({ step, data, onComplete, loading }: WorkflowStepInputProps) {
  const [stepData, setStepData] = useState<any>({});
  
  const Component = step.ui?.component
    ? componentRegistry.resolve(step.ui.component)
    : null;

  const handleSubmit = () => {
    onComplete(stepData);
  };

  if (!Component) {
    return (
      <div className="text-center py-8">
        <p className="text-gray-600 mb-4">Input step: {step.id}</p>
        <textarea
          className="w-full px-3 py-2 border border-gray-300 rounded-md mb-4"
          rows={4}
          value={JSON.stringify(stepData, null, 2)}
          onChange={(e) => {
            try {
              setStepData(JSON.parse(e.target.value));
            } catch (err) {
              // Invalid JSON, ignore
            }
          }}
          placeholder="Enter JSON data for this step"
        />
        <button
          onClick={handleSubmit}
          disabled={loading}
          className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
        >
          Continue
        </button>
      </div>
    );
  }

  return (
    <div>
      <Component
        {...(step.ui?.props || {})}
        data={data}
        onChange={setStepData}
      />
      <button
        onClick={handleSubmit}
        disabled={loading}
        className="mt-4 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 w-full"
      >
        Continue
      </button>
    </div>
  );
}
