import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { authApi, LoginRequest, RegisterRequest, ChangePasswordRequest, UpdateProfileRequest } from '../api/auth/authApi';
import { useAuthStore } from '../store/authStore';

export function useAuth() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { user, isAuthenticated, setAuth, clearAuth } = useAuthStore();

  // Login mutation
  const loginMutation = useMutation({
    mutationFn: (data: LoginRequest) => authApi.login(data),
    onSuccess: (data) => {
      setAuth(data.user, data.accessToken, data.refreshToken);
      queryClient.invalidateQueries({ queryKey: ['user'] });
      router.push('/dashboard');
    },
  });

  // Register mutation
  const registerMutation = useMutation({
    mutationFn: (data: RegisterRequest) => authApi.register(data),
    onSuccess: (data) => {
      setAuth(data.user, data.accessToken, data.refreshToken);
      queryClient.invalidateQueries({ queryKey: ['user'] });
      router.push('/dashboard');
    },
  });

  // Logout mutation
  const logoutMutation = useMutation({
    mutationFn: () => authApi.logout(),
    onSuccess: () => {
      clearAuth();
      queryClient.clear();
      router.push('/login');
    },
  });

  // Get current user query
  const { data: currentUser, isLoading: isLoadingUser } = useQuery({
    queryKey: ['user', user?.id],
    queryFn: () => authApi.getCurrentUser(user!.id),
    enabled: !!user?.id && isAuthenticated,
  });

  return {
    user: currentUser || user,
    isAuthenticated,
    isLoading: isLoadingUser,
    login: loginMutation.mutate,
    loginAsync: loginMutation.mutateAsync,
    isLoggingIn: loginMutation.isPending,
    loginError: loginMutation.error,
    register: registerMutation.mutate,
    registerAsync: registerMutation.mutateAsync,
    isRegistering: registerMutation.isPending,
    registerError: registerMutation.error,
    logout: logoutMutation.mutate,
    isLoggingOut: logoutMutation.isPending,
  };
}

export function useUpdateProfile() {
  const queryClient = useQueryClient();
  const { user, updateUser } = useAuthStore();

  return useMutation({
    mutationFn: (data: UpdateProfileRequest) => {
      if (!user?.id) {
        throw new Error('User not authenticated');
      }
      return authApi.updateProfile(user.id, data);
    },
    onSuccess: (updatedProfile) => {
      updateUser(updatedProfile);
      queryClient.invalidateQueries({ queryKey: ['user'] });
    },
  });
}

export function useChangePassword() {
  const { user } = useAuthStore();

  return useMutation({
    mutationFn: (data: ChangePasswordRequest) => {
      if (!user?.id) {
        throw new Error('User not authenticated');
      }
      return authApi.changePassword(user.id, data);
    },
  });
}

export function usePasswordReset() {
  return useMutation({
    mutationFn: (email: string) => authApi.requestPasswordReset(email),
  });
}
