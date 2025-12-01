export interface ConfigRequest {
  deviceId: string;
  temperatureLimit: number;
  highToleranceEnabled: boolean;
  highToleranceReason: string | null;
  readingIntervalSeconds: number;
}

export type ConfigResponse =
  Omit<ConfigRequest, "readingIntervalSeconds"> & {
      readingIntervalMs: number;
  };


const BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL; 

export const ConfigService = {

  get: async (deviceId: string): Promise<ConfigResponse | null> => {
    try {
      const res = await fetch(`${BASE_URL}/api/config/${deviceId}`);

      if (res.status === 404) return null;
      if (!res.ok) throw new Error('Failed to fetch configuration');
      return await res.json();

    } catch (error) {
      throw error;
    }
  },

  save: async (config: ConfigRequest, isUpdate: boolean) => {
    const method = isUpdate ? 'PUT' : 'POST';

    const res = await fetch(`${BASE_URL}/api/config`, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(config),
    });

    if (!res.ok) throw new Error('Failed to save configuration');
    return await res.json();
  },

  reset: async (deviceId: string): Promise<ConfigResponse> => {
    const res = await fetch(`${BASE_URL}/api/config/${deviceId}/reset`, {
      method: 'PUT',
    });
    if (!res.ok) throw new Error('Failed to reset configuration');
    return await res.json();
  }
};