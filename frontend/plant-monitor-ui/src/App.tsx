import { useState, useEffect, useMemo } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Activity } from 'lucide-react';


// DISCLIAMER: The project is focusing on backend practicing, so this is largely AI generated code.


// --- CONFIGURATION ---
const USE_DUMMY_DATA = false; // Set to 'true' to test the UI without the backend
const BASE_URL = import.meta.env.VITE_API_BASE_URL;

const API_ENDPOINTS = {
  ALL: `${BASE_URL}/plantdata`,
  MONTH: `${BASE_URL}/plantdata/month`,
  WEEK: `${BASE_URL}/plantdata/week`
};

// --- TYPES (Interfaces) ---
// This tells TypeScript exactly what fields to expect in our data objects
interface PlantReading {
  id?: number;
  reading: number; // Matches the 'reading' field in your Java Entity
  date: string;    // Matches the 'date' field in your Java Entity
}

function App() {
  // --- STATE (The app's memory) ---
  // readings: stores the array of data from the API
  const [readings, setReadings] = useState<PlantReading[]>([]);
  // view: keeps track of which time filter is active (All, 30 Days, 7 Days)
  const [view, setView] = useState<'ALL' | '30D' | '7D'>('ALL');
  // loading: used to show a message while waiting for the backend
  const [loading, setLoading] = useState(true);

  // --- FUNCTIONS ---

  /**
   * FetchData: The core function that talks to Spring Boot.
   * 'async/await' is a modern way to handle tasks that take time (like networking).
   */
  const fetchData = async () => {
    if (USE_DUMMY_DATA) {
      setReadings(generateMockData());
      setLoading(false);
      return;
    }

    try {
      setLoading(true);

      let url; // Use a variable for the URL to make it cleaner
      switch (view) {
        case '30D': url = API_ENDPOINTS.MONTH; break;
        case '7D': url = API_ENDPOINTS.WEEK; break;
        default: url = API_ENDPOINTS.ALL; break;
      }

      let response
      switch (view) {
        case '30D':
          response = await fetch(API_ENDPOINTS.MONTH)
          break
        case '7D':
          response = await fetch(API_ENDPOINTS.WEEK)
          break
        default:
          response = await fetch(API_ENDPOINTS.ALL)
          break
      }

      console.log(`Fetching from: ${url}`);

      if (!response.ok) throw new Error("Backend unreachable");
      const data = await response.json();
      setReadings(data);
    } catch (err) {
      console.error("Fetch error:", err);
    } finally {
      setLoading(false);
    }
  };

  /**
     * We add [view] to the dependency array.
     * This tells React: "Whenever the user clicks a different filter button,
     * run fetchData() automatically."
     */
  useEffect(() => {
    fetchData();
  }, [view]);

  /**
     * Since the Backend is now filtering the data for us, 
     * we don't need the complex math in useMemo anymore!
     * We just use the 'readings' array directly.
     */
  const filteredData = readings;

  // --- UI RENDER ---
  return (
    <div style={{ maxWidth: '900px', margin: '0 auto', padding: '2rem', fontFamily: 'sans-serif' }}>

      <header style={{ textAlign: 'center' }}>
        <h1 style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px' }}>
          <Activity color="#22c55e" /> Plant Monitor
        </h1>
        {USE_DUMMY_DATA && <p style={{ color: 'orange' }}>⚠️ Running in Demo Mode (Dummy Data)</p>}
      </header>

      {/* Filter Buttons */}
      <div style={{ display: 'flex', justifyContent: 'center', gap: '10px', margin: '20px 0' }}>
        {['ALL', '30D', '7D'].map((v) => (
          <button
            key={v}
            onClick={() => setView(v as any)}
            style={buttonStyle(view === v)}
          >
            {v === 'ALL' ? 'All Time' : `Last ${v}`}
          </button>
        ))}
        <button onClick={fetchData} style={{ marginLeft: '20px' }}>🔄 Refresh</button>
      </div>

      {/* Chart Section */}
      <div style={{ background: '#fff', padding: '1.5rem', borderRadius: '12px', boxShadow: '0 4px 10px rgba(0,0,0,0.1)', height: '400px' }}>
        {loading ? (
          <p>Loading data from backend...</p>
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={filteredData}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis
                dataKey="date"
                tickFormatter={(val) => new Date(val).toLocaleDateString()}
              />
              <YAxis unit="" />
              <Tooltip labelFormatter={(label) => new Date(label).toLocaleString()} />
              <Line
                type="monotone"
                dataKey="reading"
                stroke="#22c55e"
                strokeWidth={3}
                dot={false}
              />
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
}

// --- HELPER STYLING ---
const buttonStyle = (active: boolean) => ({
  padding: '8px 16px',
  borderRadius: '20px',
  cursor: 'pointer',
  border: '1px solid #22c55e',
  backgroundColor: active ? '#22c55e' : '#fff',
  color: active ? '#fff' : '#22c55e',
  fontWeight: 'bold'
});

// --- DUMMY DATA GEN ---
function generateMockData(): PlantReading[] {
  return Array.from({ length: 50 }, (_, i) => ({
    reading: Math.floor(Math.random() * 10) + 20,
    date: new Date(Date.now() - i * 3600000 * 24).toISOString()
  })).reverse();
}

export default App;