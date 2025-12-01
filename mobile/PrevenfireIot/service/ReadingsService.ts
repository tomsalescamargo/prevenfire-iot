export interface Reading {
  id: number;
  deviceId: string;
  temperature: number;
  temperatureLimit: number;
  isOverLimit: boolean;
  timestamp: string;
}

const BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL;

export const ReadingsService = {
  getAll: async (deviceId: string): Promise<Reading[]> => {
    const res = await fetch(`${BASE_URL}/api/readings/${deviceId}`);

    if (res.status === 404) {
      throw new Error('Device not found');
    }
    if (!res.ok) {
      throw new Error('Failed to fetch readings');
    }

    return res.json();
  },

  getCriticals: async (deviceId: string): Promise<Reading[]> => {
    const res = await fetch(`${BASE_URL}/api/readings/${deviceId}/criticals`);

    if (res.status === 404) {
      throw new Error('Device not found');
    }
    if (!res.ok) {
      throw new Error('Failed to fetch critical readings');
    }

    return res.json();
  }
};