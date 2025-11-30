import { Tabs } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';

export default function TabLayout() {
  return (
    <Tabs 
      screenOptions={{ 
        headerShown: false,
        tabBarActiveTintColor: '#dc2626',
        tabBarInactiveTintColor: '#777',
      }}
    >
      <Tabs.Screen 
        name="readings" 
        options={{ 
          title: 'Monitoramentos',
          tabBarIcon: ({ color, size }) => (
            <Ionicons name="flame-outline" size={size} color={color} />
          ),
        }} 
      />

      <Tabs.Screen 
        name="index" 
        options={{ 
          title: 'Home',
          tabBarIcon: ({ color, size }) => (
            <Ionicons name="home-outline" size={size} color={color} />
          ),
        }} 
      />

      <Tabs.Screen 
        name="configs" 
        options={{ 
          title: 'Configurações',
          tabBarIcon: ({ color, size }) => (
            <Ionicons name="settings-outline" size={size} color={color} />
          ),
        }} 
      />

    </Tabs>
  );
}
