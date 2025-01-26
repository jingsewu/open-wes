import React, {useState} from 'react';
import {
    Database,
    Search,
    LineChart,
    BarChart,
    PieChart,
    Table2,
    Download,
    LayoutDashboard,
} from 'lucide-react';
import {ResultVisualizations} from './components/ResultVisualizations';
import {DashboardGrid} from './components/DashboardGrid';
import {QueryResult, QueryError, Dashboard, DashboardWidget} from './types';
import request from "@/utils/requestInterceptor";

function App() {
    const [query, setQuery] = useState('');
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState<QueryResult | null>(null);
    const [error, setError] = useState<QueryError | null>(null);
    const [activeView, setActiveView] = useState<'table' | 'line' | 'bar' | 'pie'>('table');
    const [showDashboard, setShowDashboard] = useState(false);
    const [dashboard, setDashboard] = useState<Dashboard>({
        id: 'default',
        name: 'My Dashboard',
        widgets: [],
    });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!query.trim() || loading) return;

        setLoading(true);
        setError(null);
        setResult(null);

        try {

            const response: any = await request({
                method: "post",
                url: `/ai/ai/analysis`,
                headers: {
                    'Content-Type': 'application/json',
                    'Access-Control-Allow-Headers': 'Content-Type',
                },
                data: {query: query.trim()}
            });

            debugger
            const data = await response.data;
            setResult(data);
        } catch (error) {
            setError({
                message: `Unable to execute query. ${
                    error instanceof Error ? error.message : ''
                }`,
            });
        } finally {
            setLoading(false);
        }
    };

    const handleExport = (format: 'csv' | 'json') => {
        if (!result) return;

        let content: string;
        let filename: string;
        let type: string;

        if (format === 'csv') {
            const headers = result.columns.map(col => col.name).join(',');
            const rows = result.data.map(row =>
                Object.values(row).map(cell => `"${cell}"`).join(',')
            );
            content = [headers, ...rows].join('\n');
            filename = 'query_results.csv';
            type = 'text/csv';
        } else {
            content = JSON.stringify(result.data, null, 2);
            filename = 'query_results.json';
            type = 'application/json';
        }

        const blob = new Blob([content], {type});
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
    };

    const generateUUID = () => {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
            const r = (Math.random() * 16) | 0;
            const v = c === 'x' ? r : (r & 0x3) | 0x8;
            return v.toString(16);
        });
    }

    const handleAddWidget = () => {
        if (!result) return;

        const newWidget: DashboardWidget = {
            id: generateUUID(),
            type: activeView === 'table' ? 'table' : 'chart',
            title: 'New Widget',
            query: query,
            visualization: activeView === 'table' ? undefined : activeView,
            result: result,
            insights: ['Trend shows consistent growth', 'Unusual spike detected in recent data'],
        };

        setDashboard({
            ...dashboard,
            widgets: [...dashboard.widgets, newWidget],
        });
    };

    return (
        <div className="flex min-h-screen flex-col bg-gray-50">
            {/* Header */}
            <header className="bg-white shadow">
                <div className="mx-auto max-w-7xl px-4 py-6">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <Database className="h-8 w-8 text-blue-600"/>
                            <h1 className="text-xl font-semibold text-gray-900">
                                Natural Language SQL Query
                            </h1>
                        </div>
                        <button
                            onClick={() => setShowDashboard(!showDashboard)}
                            className={`flex items-center gap-2 rounded-lg px-4 py-2 ${
                                showDashboard
                                    ? 'bg-blue-100 text-blue-700'
                                    : 'text-gray-600 hover:bg-gray-100'
                            }`}
                        >
                            <LayoutDashboard className="h-5 w-5"/>
                            <span>Dashboard</span>
                        </button>
                    </div>
                </div>
            </header>

            {/* Main content */}
            <main className="flex-1 py-8">
                <div className="mx-auto max-w-7xl px-4">
                    {showDashboard ? (
                        <DashboardGrid
                            dashboard={dashboard}
                            onDashboardChange={setDashboard}
                            onAddWidget={handleAddWidget}
                        />
                    ) : (
                        <>
                            {/* Query Input */}
                            <form onSubmit={handleSubmit} className="mb-8">
                                <div className="flex flex-col gap-2">
                                    <label htmlFor="query" className="text-sm font-medium text-gray-700">
                                        Describe your query in natural language
                                    </label>
                                    <div className="flex gap-2">
                                        <input
                                            type="text"
                                            id="query"
                                            value={query}
                                            onChange={(e) => setQuery(e.target.value)}
                                            placeholder="e.g., Show me all users who signed up last month"
                                            className="flex-1 rounded-lg border border-gray-300 px-4 py-2 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/40"
                                            disabled={loading}
                                        />
                                        <button
                                            type="submit"
                                            disabled={loading || !query.trim()}
                                            className="flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-white transition-colors hover:bg-blue-700 disabled:opacity-50"
                                        >
                                            <Search className="h-5 w-5"/>
                                            <span>Query</span>
                                        </button>

                                    </div>
                                </div>
                            </form>

                            {/* Loading State */}
                            {loading && (
                                <div className="flex items-center justify-center py-8">
                                    <div className="flex items-center gap-2 text-sm text-gray-500">
                                        <div
                                            className="h-2 w-2 animate-bounce rounded-full bg-blue-400 [animation-delay:-0.3s]"></div>
                                        <div
                                            className="h-2 w-2 animate-bounce rounded-full bg-blue-400 [animation-delay:-0.15s]"></div>
                                        <div className="h-2 w-2 animate-bounce rounded-full bg-blue-400"></div>
                                    </div>
                                </div>
                            )}

                            {/* Error State */}
                            {error && (
                                <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-red-700">
                                    <p className="font-medium">Error</p>
                                    <p className="mt-1">{error.message}</p>
                                    {error.sql && (
                                        <pre className="mt-2 rounded bg-red-100 p-2 text-sm">
                      {error.sql}
                    </pre>
                                    )}
                                </div>
                            )}

                            {/* Results */}
                            {result && (
                                <div className="space-y-4">
                                    {/* Generated SQL */}
                                    <div className="rounded-lg border border-gray-200 bg-white p-4">
                                        <h2 className="mb-2 font-medium text-gray-700">Generated SQL</h2>
                                        <pre className="rounded bg-gray-50 p-3 text-sm text-gray-800">
                      {result.sql}
                    </pre>
                                    </div>

                                    {/* View Controls */}
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-2">
                                            <button
                                                onClick={() => setActiveView('table')}
                                                className={`flex items-center gap-1 rounded-lg px-3 py-1.5 text-sm ${
                                                    activeView === 'table'
                                                        ? 'bg-blue-100 text-blue-700'
                                                        : 'text-gray-600 hover:bg-gray-100'
                                                }`}
                                            >
                                                <Table2 className="h-4 w-4"/>
                                                <span>Table</span>
                                            </button>
                                            <button
                                                onClick={() => setActiveView('line')}
                                                className={`flex items-center gap-1 rounded-lg px-3 py-1.5 text-sm ${
                                                    activeView === 'line'
                                                        ? 'bg-blue-100 text-blue-700'
                                                        : 'text-gray-600 hover:bg-gray-100'
                                                }`}
                                            >
                                                <LineChart className="h-4 w-4"/>
                                                <span>Line</span>
                                            </button>
                                            <button
                                                onClick={() => setActiveView('bar')}
                                                className={`flex items-center gap-1 rounded-lg px-3 py-1.5 text-sm ${
                                                    activeView === 'bar'
                                                        ? 'bg-blue-100 text-blue-700'
                                                        : 'text-gray-600 hover:bg-gray-100'
                                                }`}
                                            >
                                                <BarChart className="h-4 w-4"/>
                                                <span>Bar</span>
                                            </button>
                                            <button
                                                onClick={() => setActiveView('pie')}
                                                className={`flex items-center gap-1 rounded-lg px-3 py-1.5 text-sm ${
                                                    activeView === 'pie'
                                                        ? 'bg-blue-100 text-blue-700'
                                                        : 'text-gray-600 hover:bg-gray-100'
                                                }`}
                                            >
                                                <PieChart className="h-4 w-4"/>
                                                <span>Pie</span>
                                            </button>
                                        </div>

                                        <div className="flex items-center gap-2">
                                            <button
                                                onClick={() => handleAddWidget()}
                                                className="flex items-center gap-1 rounded-lg px-3 py-1.5 text-sm text-blue-600 hover:bg-blue-50"
                                            >
                                                <LayoutDashboard className="h-4 w-4"/>
                                                <span>Add to Dashboard</span>
                                            </button>
                                            <button
                                                onClick={() => handleExport('csv')}
                                                className="flex items-center gap-1 rounded-lg px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-100"
                                            >
                                                <Download className="h-4 w-4"/>
                                                <span>Export CSV</span>
                                            </button>
                                            <button
                                                onClick={() => handleExport('json')}
                                                className="flex items-center gap-1 rounded-lg px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-100"
                                            >
                                                <Download className="h-4 w-4"/>
                                                <span>Export JSON</span>
                                            </button>
                                        </div>
                                    </div>

                                    {/* Results View */}
                                    <div className="rounded-lg border border-gray-200 bg-white">
                                        {activeView === 'table' ? (
                                            <div className="overflow-x-auto">
                                                <table className="w-full min-w-full divide-y divide-gray-200">
                                                    <thead className="bg-gray-50">
                                                    <tr>
                                                        {result.columns.map((column, i) => (
                                                            <th
                                                                key={i}
                                                                className="group relative px-6 py-3 text-left"
                                                            >
                                                                <div className="flex items-center gap-2">
                                    <span className="text-xs font-medium uppercase tracking-wider text-gray-500">
                                      {column.name}
                                    </span>
                                                                    {column.description && (
                                                                        <div className="relative">
                                                                            <div
                                                                                className="invisible absolute bottom-full left-1/2 mb-2 -translate-x-1/2 rounded-lg bg-gray-900 px-2 py-1 text-xs text-white opacity-0 transition-all group-hover:visible group-hover:opacity-100">
                                                                                <div
                                                                                    className="font-medium">{column.dataType}</div>
                                                                                <div
                                                                                    className="text-gray-300">{column.description}</div>
                                                                            </div>
                                                                        </div>
                                                                    )}
                                                                </div>
                                                            </th>
                                                        ))}
                                                    </tr>
                                                    </thead>
                                                    <tbody className="divide-y divide-gray-200 bg-white">
                                                    {result.data.map((row, i) => (
                                                        <tr key={i}>
                                                            {Object.values(row).map((cell: any, j) => (
                                                                <td
                                                                    key={j}
                                                                    className="whitespace-nowrap px-6 py-4 text-sm text-gray-900"
                                                                >
                                                                    {cell?.toString() ?? ''}
                                                                </td>
                                                            ))}
                                                        </tr>
                                                    ))}
                                                    </tbody>
                                                </table>
                                            </div>
                                        ) : (
                                            <div className="p-4">
                                                <ResultVisualizations
                                                    result={result}
                                                    activeChart={activeView === 'table' ? null : activeView}
                                                />
                                            </div>
                                        )}
                                    </div>
                                </div>
                            )}
                        </>
                    )}
                </div>
            </main>
        </div>
    );
}

export default App;
