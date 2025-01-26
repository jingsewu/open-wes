import React, { useEffect, useState } from 'react';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragEndEvent,
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  rectSortingStrategy,
} from '@dnd-kit/sortable';
import { DashboardWidget } from './DashboardWidget';
import { Dashboard } from '../types';
import { Plus, Edit2, Check } from 'lucide-react';

interface DashboardGridProps {
  dashboard: Dashboard;
  onDashboardChange: (dashboard: Dashboard) => void;
  onAddWidget: () => void;
  className?: string;
}

export function DashboardGrid({ dashboard, onDashboardChange, onAddWidget, className = '' }: DashboardGridProps) {
  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [tempTitle, setTempTitle] = useState(dashboard.name);

  const sensors = useSensors(
      useSensor(PointerSensor),
      useSensor(KeyboardSensor, {
        coordinateGetter: sortableKeyboardCoordinates,
      })
  );

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (over && active.id !== over.id) {
      const oldIndex = dashboard.widgets.findIndex((w) => w.id === active.id);
      const newIndex = dashboard.widgets.findIndex((w) => w.id === over.id);

      const updatedDashboard = {
        ...dashboard,
        widgets: arrayMove(dashboard.widgets, oldIndex, newIndex),
      };

      onDashboardChange(updatedDashboard);
      saveDashboard(updatedDashboard);
    }
  };

  const saveDashboard = async (updatedDashboard: Dashboard) => {
    try {
      const response = await fetch('/api/dashboards', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(updatedDashboard),
      });

      if (!response.ok) {
        throw new Error('Failed to save dashboard');
      }
    } catch (error) {
      console.error('Error saving dashboard:', error);
    }
  };

  const handleTitleSave = () => {
    if (tempTitle.trim() !== '') {
      const updatedDashboard = {
        ...dashboard,
        name: tempTitle.trim()
      };
      onDashboardChange(updatedDashboard);
      saveDashboard(updatedDashboard);
      setIsEditingTitle(false);
    }
  };

  const handleTitleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleTitleSave();
    } else if (e.key === 'Escape') {
      setTempTitle(dashboard.name);
      setIsEditingTitle(false);
    }
  };

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        const response = await fetch(`/api/dashboards/${dashboard.id}`);
        if (response.ok) {
          const data = await response.json();
          onDashboardChange(data);
          setTempTitle(data.name);
        }
      } catch (error) {
        console.error('Error loading dashboard:', error);
      }
    };

    loadDashboard();
  }, [dashboard.id]);

  return (
      <div className={`p-4 ${className}`}>
        <div className="mb-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            {isEditingTitle ? (
                <div className="flex items-center gap-2">
                  <input
                      type="text"
                      value={tempTitle}
                      onChange={(e) => setTempTitle(e.target.value)}
                      onKeyDown={handleTitleKeyDown}
                      className="rounded-lg border border-gray-300 px-3 py-1 text-xl font-semibold focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/40"
                      autoFocus
                      placeholder="Dashboard Title"
                  />
                  <button
                      onClick={handleTitleSave}
                      className="rounded-lg p-1 text-green-600 hover:bg-green-50"
                      title="Save title"
                  >
                    <Check className="h-5 w-5" />
                  </button>
                </div>
            ) : (
                <div className="flex items-center gap-2">
                  <h2 className="text-xl font-semibold text-gray-900">{dashboard.name}</h2>
                  <button
                      onClick={() => setIsEditingTitle(true)}
                      className="rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
                      title="Edit dashboard title"
                  >
                    <Edit2 className="h-4 w-4" />
                  </button>
                </div>
            )}
          </div>
          <button
              onClick={onAddWidget}
              className="flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-white transition-colors hover:bg-blue-700"
          >
            <Plus className="h-5 w-5" />
            <span>Add Widget</span>
          </button>
        </div>

        <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            onDragEnd={handleDragEnd}
        >
          <SortableContext items={dashboard.widgets.map((w) => w.id)} strategy={rectSortingStrategy}>
            <div className="grid grid-cols-12 gap-4">
              {dashboard.widgets.map((widget) => {
                const widgetSize = widget.size || { w: 4, h: 1 }; // Default size
                return (
                    <div
                        key={widget.id}
                        className={`col-span-${widgetSize.w} row-span-${widgetSize.h}`}
                        style={{
                          gridColumn: `span ${widgetSize.w}`,
                          gridRow: `span ${widgetSize.h}`,
                        }}
                    >
                      <DashboardWidget
                          widget={widget}
                          onUpdate={(updatedWidget) => {
                            const updatedWidgets = dashboard.widgets.map((w) =>
                                w.id === updatedWidget.id ? updatedWidget : w
                            );
                            const updatedDashboard = { ...dashboard, widgets: updatedWidgets };
                            onDashboardChange(updatedDashboard);
                            saveDashboard(updatedDashboard);
                          }}
                          onDelete={() => {
                            const updatedWidgets = dashboard.widgets.filter((w) => w.id !== widget.id);
                            const updatedDashboard = { ...dashboard, widgets: updatedWidgets };
                            onDashboardChange(updatedDashboard);
                            saveDashboard(updatedDashboard);
                          }}
                          onResize={(newSize) => {
                            const updatedWidget = { ...widget, size: newSize };
                            const updatedWidgets = dashboard.widgets.map((w) =>
                                w.id === widget.id ? updatedWidget : w
                            );
                            const updatedDashboard = { ...dashboard, widgets: updatedWidgets };
                            onDashboardChange(updatedDashboard);
                            saveDashboard(updatedDashboard);
                          }}
                      />
                    </div>
                );
              })}
            </div>
          </SortableContext>
        </DndContext>
      </div>
  );
}
