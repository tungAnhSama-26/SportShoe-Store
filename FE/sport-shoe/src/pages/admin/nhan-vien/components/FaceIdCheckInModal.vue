<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { ScanFace, X } from 'lucide-vue-next';
import Button from '../../../../components/ui/Button.vue';

const props = defineProps({
  show: Boolean,
  savedDescriptorString: String // JSON string of the 128-float array
});

const emit = defineEmits(['close', 'success']);

const isModelsLoaded = ref(false);
const isLoading = ref(false);
const loadingMessage = ref('');
const errorMessage = ref('');

const videoRef = ref(null);
const canvasRef = ref(null);
let stream = null;
let detectionInterval = null;
let faceapi = null;
let faceapiLoader = null;

async function loadFaceApi() {
  if (!faceapiLoader) {
    faceapiLoader = import('face-api.js');
  }
  faceapi = faceapi ?? await faceapiLoader;
  return faceapi;
}

async function loadModels() {
  if (isModelsLoaded.value) return;
  isLoading.value = true;
  loadingMessage.value = 'Đang khởi động Camera và AI...';
  try {
    await loadFaceApi();
    const MODEL_URL = '/models';
    await Promise.all([
      faceapi.nets.tinyFaceDetector.loadFromUri(MODEL_URL),
      faceapi.nets.faceLandmark68Net.loadFromUri(MODEL_URL),
      faceapi.nets.faceRecognitionNet.loadFromUri(MODEL_URL),
    ]);
    isModelsLoaded.value = true;
  } catch (error) {
    errorMessage.value = 'Lỗi hệ thống AI. Vui lòng liên hệ quản trị viên.';
    console.error('Error loading faceapi models:', error);
  } finally {
    isLoading.value = false;
  }
}

async function startWebcam() {
  await loadModels();
  if (!isModelsLoaded.value) return;
  
  errorMessage.value = '';
  await nextTick();
  
  try {
    stream = await navigator.mediaDevices.getUserMedia({ video: {} });
    if (videoRef.value) {
      videoRef.value.srcObject = stream;
    }
  } catch (error) {
    errorMessage.value = 'Không thể mở webcam. Vui lòng cấp quyền truy cập máy ảnh.';
    console.error(error);
  }
}

function stopWebcam() {
  if (stream) {
    stream.getTracks().forEach(track => track.stop());
    stream = null;
  }
  if (detectionInterval) {
    clearInterval(detectionInterval);
    detectionInterval = null;
  }
}

async function handleVideoPlay() {
  if (!videoRef.value || !canvasRef.value) return;
  if (!props.savedDescriptorString) {
    errorMessage.value = "Tài khoản của bạn chưa đăng ký Face ID. Vui lòng liên hệ Admin.";
    return;
  }
  
  let savedDescriptor;
  try {
    savedDescriptor = new Float32Array(JSON.parse(props.savedDescriptorString));
  } catch (e) {
    errorMessage.value = "Dữ liệu Face ID bị lỗi.";
    return;
  }

  const displaySize = { width: videoRef.value.videoWidth, height: videoRef.value.videoHeight };
  faceapi.matchDimensions(canvasRef.value, displaySize);

  detectionInterval = setInterval(async () => {
    if (!videoRef.value) return;
    
    const detections = await faceapi.detectAllFaces(videoRef.value, new faceapi.TinyFaceDetectorOptions())
                                    .withFaceLandmarks()
                                    .withFaceDescriptors();
    
    if (canvasRef.value) {
      const resizedDetections = faceapi.resizeResults(detections, displaySize);
      const ctx = canvasRef.value.getContext('2d');
      ctx.clearRect(0, 0, canvasRef.value.width, canvasRef.value.height);
      faceapi.draw.drawDetections(canvasRef.value, resizedDetections);
    }
    
    if (detections.length === 1) {
      const newDescriptor = detections[0].descriptor;
      const distance = faceapi.euclideanDistance(savedDescriptor, newDescriptor);
      
      // Nếu khoảng cách < 0.5 (hoặc 0.45 để chặt chẽ hơn) -> đúng người
      if (distance < 0.45) {
        stopWebcam();
        emit('success');
      }
    }
  }, 300); // Quét mỗi 300ms
}

function closeModal() {
  stopWebcam();
  errorMessage.value = '';
  emit('close');
}

onMounted(() => {
  if (props.show) startWebcam();
});

import { watch } from 'vue';
watch(() => props.show, (newVal) => {
  if (newVal) {
    startWebcam();
  } else {
    stopWebcam();
  }
});

onUnmounted(() => {
  stopWebcam();
});

</script>

<template>
  <Teleport to="body">
    <div v-if="show" class="fixed inset-0 z-[100] flex items-center justify-center bg-black/80 backdrop-blur-sm">
      <div class="relative w-full max-w-md rounded-[28px] bg-white p-6 shadow-2xl mx-4 text-center">
        <!-- Header -->
        <div class="mb-5 flex items-center justify-between">
          <h3 class="text-[18px] font-bold text-slate-900">Check-in Face ID</h3>
          <button type="button" @click="closeModal"
            class="flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition">
            <X class="h-4 w-4" />
          </button>
        </div>
        
        <!-- Loading -->
        <div v-if="isLoading" class="py-12 space-y-4">
          <div class="inline-flex h-12 w-12 animate-spin items-center justify-center rounded-full border-4 border-slate-200 border-t-primary"></div>
          <p class="text-sm font-medium text-slate-600">{{ loadingMessage }}</p>
        </div>
        
        <!-- Error -->
        <div v-if="errorMessage" class="mb-4 rounded-2xl bg-rose-50 p-4 text-sm font-medium text-rose-600 border border-rose-100 text-left flex items-start gap-2">
          <X class="h-5 w-5 shrink-0" />
          <span>{{ errorMessage }}</span>
        </div>
        
        <!-- Camera -->
        <div v-if="!isLoading" class="relative overflow-hidden rounded-[20px] bg-black" style="aspect-ratio:3/4">
           <video ref="videoRef" @play="handleVideoPlay" class="absolute inset-0 w-full h-full object-cover" autoplay playsinline muted></video>
           <canvas ref="canvasRef" class="absolute inset-0 pointer-events-none"></canvas>
           
           <div class="absolute inset-0 flex flex-col items-center justify-end pb-8 pointer-events-none">
              <!-- Overlay face guide -->
              <div class="w-48 h-64 border-[3px] border-dashed border-white/40 rounded-[100px] mb-8 relative">
                 <div class="absolute inset-x-0 h-[2px] bg-primary/70 shadow-[0_0_8px_rgba(14,165,233,0.8)] animate-pulse" style="top: 30%"></div>
              </div>
              <div class="px-4 py-2 rounded-full bg-black/50 backdrop-blur-md text-white text-xs font-semibold flex items-center gap-2">
                 <ScanFace class="h-4 w-4" />
                 Nhìn thẳng vào Camera
              </div>
           </div>
        </div>
        
      </div>
    </div>
  </Teleport>
</template>
