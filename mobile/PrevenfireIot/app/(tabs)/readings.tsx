import { View, Text, StyleSheet } from 'react-native';

export default function ReadingsScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Monitoramento</Text>
      <Text style={styles.subtitle}>Tela de monitoramento em desenvolvimento</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { 
    flex: 1, 
    justifyContent: 'center', 
    alignItems: 'center', 
    backgroundColor: '#fff', 
    padding: 20 
  },
  title: { 
    fontSize: 32, 
    fontWeight: 'bold', 
    color: '#dc2626', 
    marginBottom: 8 
  },
  subtitle: { 
    fontSize: 18, 
    color: '#666', 
    textAlign: 'center' 
  }
});

