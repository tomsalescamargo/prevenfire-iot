import { useEffect, useState, useCallback, useRef } from 'react';
import { 
  View, Text, TextInput, ActivityIndicator,
  Switch, FlatList, RefreshControl, Linking, TouchableOpacity, Alert 
} from 'react-native';
import tw from 'twrnc';

import { Reading, ReadingsService } from '@/service/ReadingsService';
import { useDebounce } from '@/hooks/useDebounce';

import { MaterialIcons, MaterialCommunityIcons } from '@expo/vector-icons';

export default function ReadingsScreen() {
  const [deviceId, setDeviceId] = useState("");
  const debouncedId = useDebounce(deviceId, 800);

  const [onlyCriticals, setOnlyCriticals] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isValidDevice, setIsValidDevice] = useState<boolean | null>(null); 
  const [statusMsg, setStatusMsg] = useState("");

  const [deviceReadings, setDeviceReadings] = useState<Reading[]>([]);

  const formatDate = (isoString: string): string => {
    if (!isoString) return '--:--';
    const date = new Date(isoString);
    return new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  };

  const isMountedRef = useRef(true);

  const fetchReadings = useCallback(async () => {
    if (!debouncedId || debouncedId.trim() === '') return;

    if (!isMountedRef.current) return;

    setIsLoading(true);
    setStatusMsg('Buscando dados...');
    setIsValidDevice(null);

    try {
      let data: Reading[] = [];

      if (onlyCriticals) {
        data = await ReadingsService.getCriticals(debouncedId);
      } else {
        data = await ReadingsService.getAll(debouncedId);
      }

      if (!data || data.length === 0) {
        throw new Error('No data returned');
      }

      if (!isMountedRef.current) return;

      setDeviceReadings(data);
      setIsValidDevice(true);
      setStatusMsg('');

    } catch (error) {
      if (!isMountedRef.current) return;

      setIsValidDevice(false);
      setDeviceReadings([]);
      setStatusMsg('Dispositivo não encontrado');
    } finally {
      if (!isMountedRef.current) return;
      setIsLoading(false);
    }
  }, [debouncedId, onlyCriticals]);

  useEffect(() => {
    isMountedRef.current = true;

    if (!debouncedId || debouncedId.trim() === '') {
      setIsValidDevice(null);
      setDeviceReadings([]);
      setStatusMsg('');
      return;
    }

    fetchReadings();

    return () => {
      isMountedRef.current = false; // Cleanup
    };
  }, [fetchReadings]);

  const handleEmergencyCall = () => {
    Alert.alert(
      'Confirmar Ligação',
      'Deseja ligar para os Bombeiros (193)?',
      [
        { text: 'Cancelar', style: 'cancel' },
        { text: 'LIGAR', onPress: () => Linking.openURL('tel:193'), style: 'destructive' }
      ]
    );
  };

  const renderItem = ({ item }: { item: Reading }) => (
    <View 
      style={tw`bg-white rounded-xl mb-3 shadow-sm border border-gray-100 border-l-8 flex-row items-center justify-between p-4
        ${item.isOverLimit ? 'border-l-red-600' : 'border-l-blue-500'}
      `}
    >
      <View style={tw`flex-1`}>
        <View style={tw`flex-row items-center mb-1`}>
          <MaterialIcons name="access-time" size={14} color="#9CA3AF" />
          <Text style={tw`text-gray-400 text-xs ml-1 font-bold`}>
            {formatDate(item.timestamp)}
          </Text>
        </View>

        <View style={tw`flex-row items-end`}>
          <Text style={tw`text-2xl font-bold ${item.isOverLimit ? 'text-red-700' : 'text-gray-800'}`}>
            {item.temperature.toFixed(1)}°C
          </Text>
        </View>

        <Text style={tw`text-xs text-gray-500 mt-1`}>
          Limite: <Text style={tw`font-bold`}>{item.temperatureLimit}°C</Text>
        </Text>
      </View>

      {item.isOverLimit ? (
        <TouchableOpacity 
          onPress={handleEmergencyCall}
          style={tw`bg-red-50 p-3 rounded-full border border-red-100 items-center justify-center shadow-sm`}
        >
          <MaterialCommunityIcons name="phone-alert" size={28} color="#DC2626" />
        </TouchableOpacity>
      ) : (
        <View style={tw`opacity-50 w-11`}>
          <MaterialCommunityIcons name="check-circle-outline" size={32} color="#3b82f6" />
        </View>
      )} 
    </View>
  );

  const getBorderColor = (): string => {
    if (isLoading) return 'border-blue-500';
    if (isValidDevice === true) return 'border-blue-300';
    if (isValidDevice === false) return 'border-red-500';
    return 'border-gray-300';
  };

  return (
    <View style={tw`flex-1 bg-gray-50 p-6 pt-10`}>
      <Text style={tw`text-red-700 text-3xl font-bold mt-10 mb-6`}>
        Monitoramento
      </Text>

      {/* Device ID Smart Input */}
      <View style={tw`mb-4`}>
        <Text style={tw`text-gray-700 font-bold text-xs uppercase mb-2`}>
          ID do Dispositivo
        </Text>
        <View>
          <TextInput
            style={tw`border-2 rounded-xl px-4 pb-2 h-14 bg-white text-lg ${getBorderColor()}`}
            placeholder="Digite o ID (ex: ESP32-01)"
            placeholderTextColor="#9CA3AF"
            value={deviceId}
            onChangeText={setDeviceId}
            autoCapitalize="none"
            textAlignVertical="center"
          />
          {isLoading && (
            <View style={tw`absolute right-4 top-3`}>
              <ActivityIndicator color="#667" />
            </View>
          )}
        </View>

        <Text style={tw`mt-1 text-xs font-semibold h-5 ${isValidDevice ? 'text-gray-500' : 'text-red-500'}`}>
          {statusMsg}
        </Text>
      </View>

      <View style={tw`flex-row justify-between items-center bg-white p-3 rounded-xl border border-gray-200 mb-6 shadow-sm`}>
        <View style={tw`flex-row items-center gap-2`}>
          <MaterialCommunityIcons
            name="alert-decagram" 
            size={20}
            color={onlyCriticals ? '#DC2626' : '#9CA3AF'}
          />
          <Text style={tw`text-gray-700 font-semibold`}>Apenas Críticos</Text>
        </View>
        <Switch
          value={onlyCriticals}
          onValueChange={setOnlyCriticals}
          trackColor={{ false: '#e5e7eb', true: '#fca5a5' }}
          thumbColor={onlyCriticals ? '#dc2626' : '#f4f4f5'}
        />
      </View>

      <FlatList
        data={deviceReadings}
        keyExtractor={(item) => String(item.id)}
        showsVerticalScrollIndicator={false}
        contentContainerStyle={tw`pb-10`}
        renderItem={renderItem}
      
        ListEmptyComponent={
          !isLoading && debouncedId ? (
            <View style={tw`mt-10 items-center opacity-50`}>
              <MaterialCommunityIcons name="clipboard-text-off-outline" size={48} color="gray" />
              <Text style={tw`text-gray-500 text-lg mt-2`}>Sem leituras.</Text>
            </View>
          ) : null
        }

        refreshControl={
          <RefreshControl 
            refreshing={isLoading}
            onRefresh={fetchReadings}
            colors={['#dc2626']}
            tintColor="#dc2626"
          />
        }
      />
    </View>
  );
};