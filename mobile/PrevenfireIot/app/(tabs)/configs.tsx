import { useEffect, useState } from 'react';
import { 
  View, Text, TextInput, Switch, Pressable, Alert, 
  ActivityIndicator, ScrollView, KeyboardAvoidingView, Platform 
} from 'react-native';
import tw from 'twrnc';
import { ConfigService, ConfigRequest } from '@/service/ConfigService';
import { useDebounce } from '@/hooks/useDebounce';

export default function ConfigsScreen() {
  // DeviceId
  const [deviceId, setDeviceId] = useState('');
  const debouncedId = useDebounce(deviceId, 800);

  // Utils
  const [isLoading, setIsLoading] = useState(false);
  const [isEditingConfig, setIsEditingConfig] = useState(false);
  const [statusMsg, setStatusMsg] = useState('');

  // Form Data
  const [temperatureLimit, setTemperatureLimit] = useState("");
  const [highToleranceEnabled, setHighToleranceEnabled] = useState(false);
  const [highToleranceReason, setHighToleranceReason] = useState("");
  const [readingIntervalSeconds, setReadingIntervalSeconds] = useState("");


  // Debounced Automatic Search --> debouncedId
  useEffect(() => {
    if (!debouncedId || deviceId.trim() === "") {
      setStatusMsg('');
      setTemperatureLimit('');
      setReadingIntervalSeconds('');
      setIsEditingConfig(false);
      return;
    }

    let isActive = true;

    const fetchConfig = async () => {
      setIsLoading(true);
      setStatusMsg('Verificando dispositivo...');

      try {
        const data = await ConfigService.get(debouncedId);

        if (!isActive) return;

        if (data) {
          // Found Config: Edit Mode
          setIsEditingConfig(true);
          setStatusMsg('Dispositivo encontrado. Editando.');

          setTemperatureLimit(String(data.temperatureLimit));
          setHighToleranceEnabled(data.highToleranceEnabled);
          setHighToleranceReason(data.highToleranceReason || '');

          // Backend sends ms, convert to seconds for UI
          setReadingIntervalSeconds(String(data.readingIntervalMs / 1000));

        } else {
          // Not Found: Create Mode
          setIsEditingConfig(false);
          setStatusMsg("Dispositivo Novo. Preencha para criar.");

          setTemperatureLimit('');
          setHighToleranceEnabled(false);
          setHighToleranceReason('');
          setReadingIntervalSeconds('');
        }
      } catch (err) {
        if (isActive) setStatusMsg('Erro de Conexão!');
      } finally {
        if (isActive) setIsLoading(false);
      }
    };

    fetchConfig();

    return () => { isActive = false }; // Cleanup
  }, [debouncedId]);

  // Send Config
  const handleSendConfig = async () => {
    if (!debouncedId || deviceId.trim() === "") {
      Alert.alert("Erro", "Device ID é obrigatório");
      return;
    }

    // Sanitization
    const normalizedTemp = temperatureLimit.replace(',', '.');
    const cleanedTemp = normalizedTemp.replace(/[^0-9.]/g, "");

    const normalizedInterval = readingIntervalSeconds.replace(',', '.');
    const cleanedInterval = normalizedInterval.replace(/[^0-9.]/g, "");

    const tempLimitValue = parseFloat(cleanedTemp);
    const intervalValue = parseInt(cleanedInterval);

    // Logic Validation
    if (isNaN(tempLimitValue)) {
      Alert.alert("Erro", "Limite de temperatura é obrigatório e deve ser um número");
      return;
    }

    /* NOTE: Client-side Rate Limiting.
       We enforce a minimum of 10s to prevent the ESP32 from flooding the Backend
       with high-frequency write operations. While the Microservices architecture 
       technically supports millisecond-latency (for special industrial uses), 
       we restrict standard users here to protect the API Gateway and Database from saturation.
    */
    if (isNaN(intervalValue) || intervalValue < 10) {
      Alert.alert("Atenção", "Para evitar sobrecarga, o intervalo mínimo é de 10 segundos.");
      return;
    }

    const requestBody: ConfigRequest = {
      deviceId: debouncedId,
      temperatureLimit: tempLimitValue,
      highToleranceEnabled,
      highToleranceReason: highToleranceEnabled ? highToleranceReason : null,
      readingIntervalSeconds: intervalValue
    };

    try {
      await ConfigService.save(requestBody, isEditingConfig);
      Alert.alert("Sucesso ✅", `Configuração ${isEditingConfig ? "atualizada" : "criada"} com sucesso!`);
      setIsEditingConfig(true);
      setStatusMsg('Configuração salva.');
    } catch (err) {
      Alert.alert("Erro", "Falha ao salvar.");
    }
  };

  // Reset Config
  const handleResetConfig = async () => {
    try {
      const data = await ConfigService.reset(debouncedId);
      Alert.alert("Sucesso", "Configuração resetada com sucesso!");

      setIsEditingConfig(true);
      setStatusMsg('Configuração resetada.');

      // Update UI with reset data
      setTemperatureLimit(String(data.temperatureLimit));
      setHighToleranceEnabled(data.highToleranceEnabled);
      setHighToleranceReason(data.highToleranceReason || '');
      setReadingIntervalSeconds(String(data.readingIntervalMs / 1000));

    } catch (err) {
      Alert.alert("Erro", "Falha ao resetar.");
    }
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === "ios" ? "padding" : "height"}
      style={tw`flex-1 bg-white`}
    >
      <ScrollView contentContainerStyle={tw`p-6 grow`}>

        <Text style={tw`text-red-700 text-3xl font-bold mt-10 mb-8 text-center`}>
          Gerenciar Dispositivos
        </Text>

        {/* Device ID Smart Input */}
        <View style={tw`mb-2`}>
          <Text style={tw`text-gray-700 mb-2 font-bold uppercase text-xs`}>
            Id do dispositivo
          </Text>
          <View>
            <TextInput
              style={tw`border ${isEditingConfig ? 'border-green-500' : 'border-blue-500'} rounded-xl p-4 pt-1 bg-gray-50 text-lg`}
              placeholder="Digite o ID (ex: ESP32-01)"
              value={deviceId}
              onChangeText={setDeviceId}
              autoCapitalize="none"
            />
            {isLoading && (
              <View style={tw`absolute right-4 top-4`}>
                <ActivityIndicator color="#667" />
              </View>
            )}
          </View>

          <Text style={tw`mt-2 text-sm font-semibold ${isEditingConfig ? 'text-green-600' : 'text-blue-600'} h-5`}>
            {statusMsg}
          </Text>
        </View>

        <View
          style={tw`flex-1 mt-4 ${!debouncedId || isLoading ? 'opacity-80' : 'opacity-100'}`} 
          pointerEvents={(!debouncedId || isLoading) ? 'none' : 'auto'}
        >

          {/* Temperature Limit */}
          <View style={tw`mb-4`}>
            <Text style={tw`text-gray-600 mb-1 font-semibold`}>Temperatura Limite (°C)</Text>
            <TextInput
              style={tw`border border-gray-300 rounded-xl p-3 bg-white`}
              value={temperatureLimit}
              onChangeText={setTemperatureLimit}
              keyboardType='numeric'
              placeholder="Ex: 50.5"
            />
          </View>

          {/* Reading Interval Seconds */}
          <View style={tw`mb-4`}>
            <Text style={tw`text-gray-600 mb-1 font-semibold`}>Intervalo entre leituras do sensor (segundos)</Text>
            <TextInput 
                style={tw`border border-gray-300 rounded-xl p-3 bg-white`} 
                value={readingIntervalSeconds} 
                onChangeText={setReadingIntervalSeconds} 
                keyboardType="numeric"
                placeholder="Ex: 30"
            />
          </View>

          {/* High Tolerance Mode */}
            <View style={tw`mb-6 flex-row items-center align-center justify-start`}>
              <Text style={tw`text-gray-600 mr-4 font-semibold`}>
                Modo Alta Tolerância
              </Text>

              <Switch
                value={highToleranceEnabled}
                onValueChange={setHighToleranceEnabled}
                trackColor={{false: "#d1d5db", true: "#fca5a5"}}
                thumbColor={highToleranceEnabled ? "#dc2626" : "#f4f4f5"}
              />
            </View>

          {/* High Tolerance Reason */}
          {highToleranceEnabled && (
            <View style={tw`mb-4`}>
              <Text style={tw`text-gray-600 mb-1 font-semibold`}>
                Motivo do Modo Alta Tolerância
              </Text>
              <TextInput
                style={tw`border border-gray-300 rounded-xl p-3 bg-white`}
                placeholder='Ex: Cozinhando'
                value={highToleranceReason}
                onChangeText={setHighToleranceReason}
              />
            </View>
          )}

          {/* Submit Button */}
          <View style={tw`mt-6 gap-3 mb-10`}>
            <Pressable
              style={tw`bg-red-700 py-4 rounded-xl shadow-md items-center mb-4 active:opacity-80`}
              onPress={handleSendConfig}
            >
              <Text style={tw`text-white text-lg font-bold`}>
                {isEditingConfig ? 'ALTERAR CONFIGURAÇÃO' : 'CRIAR CONFIGURAÇÃO'}
              </Text>
            </Pressable>

            {isEditingConfig && (  
              <Pressable
                style={tw`bg-gray-500 mx-auto px-8 py-4 rounded-xl shadow-lg justify-center items-center`}
                onPress={handleResetConfig}
              >
                <Text style={tw`text-white font-bold`}>
                  Resetar Padrões
                </Text>
              </Pressable>
            )}
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
)};