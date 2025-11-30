import { View, Text, Pressable } from 'react-native';
import { Image } from 'expo-image';
import { useRouter } from 'expo-router';
import tw from 'twrnc';

export default function HomeScreen() {
  const router = useRouter();

  return (
    <View style={tw`flex-1 justify-center items-center bg-white p-2`}>
      <Image 
        source={require('../../assets/images/logo.png')}
        style={tw`w-full h-60 mb-8 bg-white`}
        contentFit="contain"
      />
      
      <Text style={tw`text-base text-gray-600 mb-10 text-center leading-5 px-4`}>
        Previna temperaturas indesejadas e configure o sistema conforme suas necessidades.
      </Text>

      <Pressable 
        style={tw`bg-red-700 mx-auto px-6 py-3 rounded-xl shadow-lg justify-center items-center`}
        onPress={() => router.push('./readings')}
      >
        <Text style={tw`text-white text-lg font-bold`}>Iniciar Monitoramento</Text>
      </Pressable>
    
    </View>
  );
}
