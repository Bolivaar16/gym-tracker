import React, { useRef, useState } from 'react';
import {
  ActivityIndicator, FlatList, KeyboardAvoidingView, Platform, StyleSheet,
  Text, TextInput, TouchableOpacity, View,
} from 'react-native';
import { useMutation } from '@tanstack/react-query';
import { Ionicons } from '@expo/vector-icons';
import { askCoach } from '../../api/coach';
import { colors } from '../../theme';

interface ChatMessage {
  id: string;
  role: 'user' | 'coach';
  text: string;
}

export default function CoachScreen() {
  const [messages, setMessages] = useState<ChatMessage[]>([
    { id: 'welcome', role: 'coach', text: 'Hi! Ask me anything about your training.' },
  ]);
  const [input, setInput] = useState('');
  const listRef = useRef<FlatList<ChatMessage>>(null);

  const ask = useMutation({
    mutationFn: askCoach,
    onSuccess: (res) =>
      setMessages((prev) => [...prev, { id: `c-${Date.now()}`, role: 'coach', text: res.reply }]),
    onError: () =>
      setMessages((prev) => [...prev, {
        id: `e-${Date.now()}`, role: 'coach',
        text: 'Sorry, the coach is unavailable right now. Try again later.',
      }]),
  });

  const send = () => {
    const text = input.trim();
    if (!text || ask.isPending) return;
    setMessages((prev) => [...prev, { id: `u-${Date.now()}`, role: 'user', text }]);
    setInput('');
    ask.mutate(text);
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      keyboardVerticalOffset={90}
    >
      <FlatList
        ref={listRef}
        data={messages}
        keyExtractor={(m) => m.id}
        contentContainerStyle={styles.list}
        onContentSizeChange={() => listRef.current?.scrollToEnd({ animated: true })}
        renderItem={({ item }) => (
          <View style={[styles.bubble, item.role === 'user' ? styles.userBubble : styles.coachBubble]}>
            <Text style={item.role === 'user' ? styles.userText : styles.coachText}>{item.text}</Text>
          </View>
        )}
        ListFooterComponent={ask.isPending
          ? <ActivityIndicator color={colors.primary} style={{ marginVertical: 8 }} />
          : null}
      />
      <View style={styles.inputRow}>
        <TextInput
          style={styles.input} placeholder="Ask your coach..." value={input}
          onChangeText={setInput} onSubmitEditing={send} returnKeyType="send" multiline
        />
        <TouchableOpacity style={styles.sendButton} onPress={send} disabled={ask.isPending}>
          <Ionicons name="send" size={18} color="#FFFFFF" />
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bg },
  list: { padding: 16, gap: 10 },
  bubble: { maxWidth: '82%', borderRadius: 14, padding: 12 },
  userBubble: { alignSelf: 'flex-end', backgroundColor: colors.primary },
  coachBubble: {
    alignSelf: 'flex-start', backgroundColor: colors.card,
    borderWidth: 1, borderColor: colors.border,
  },
  userText: { color: '#FFFFFF', fontSize: 15 },
  coachText: { color: colors.text, fontSize: 15 },
  inputRow: {
    flexDirection: 'row', alignItems: 'flex-end', padding: 12, gap: 8,
    backgroundColor: colors.card, borderTopWidth: 1, borderTopColor: colors.border,
  },
  input: {
    flex: 1, backgroundColor: colors.bg, borderRadius: 20, paddingHorizontal: 16,
    paddingVertical: 10, fontSize: 15, maxHeight: 100,
  },
  sendButton: {
    backgroundColor: colors.primary, borderRadius: 20, width: 40, height: 40,
    justifyContent: 'center', alignItems: 'center',
  },
});
