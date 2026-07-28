<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { Camera, X, ScanFace, CheckCircle2 } from 'lucide-vue-next';
import Button from '../../../../components/ui/Button.vue';
import { showError, showSuccess } from '../../../../utils/alert';

const props = defineProps({
  show: Boolean,
  employeeName: String,
  hasExistingFaceId: Boolean
});

const emit = defineEmits(['close', 'saved']);

const isModelsLoaded = ref(false);
const isLoading = ref(false);
const loadingMessage = ref('');
const errorMessage = ref('');

const videoRef = ref(null);
const canvasRef = ref(null);

const mode = ref('selection'); // 'selection' | 'webcam' | 'upload'
const capturedImage = ref(null);
const extractedDescriptor = ref(null);

let stream = null;
let detectionInterval = null;
let faceapi = null;
let faceapiLoader = null;
let latestDetection = null;

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
  loadingMessage.value = 'Đang tải AI models...';
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
    errorMessage.value = 'Không thể tải AI models. Đảm bảo thư mục public/models đã có đủ các file model.';
    console.error('Error loading faceapi models:', error);
  } finally {
    isLoading.value = false;
  }
}

async function startWebcam() {
  await loadModels();
  if (!isModelsLoaded.value) return;
  
  stopWebcam();
  mode.value = 'webcam';
  errorMessage.value = '';
  capturedImage.value = null;
  extractedDescriptor.value = null;
  
  await nextTick();
  
  try {
    stream = await navigator.mediaDevices.getUserMedia({ video: {} });
    if (videoRef.value) {
      videoRef.value.srcObject = stream;
    }
  } catch (error) {
    errorMessage.value = 'Không thể mở webcam. Vui lòng kiểm tra quyền truy cập.';
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

function cancelWebcam() {
  stopWebcam();
  mode.value = 'selection';
}

async function handleVideoPlay() {
  if (!videoRef.value || !canvasRef.value) return;
  if (detectionInterval) clearInterval(detectionInterval);
  
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
      faceapi.draw.drawFaceLandmarks(canvasRef.value, resizedDetections);
    }
    
    if (detections.length > 0) {
      latestDetection = detections[0];
    } else {
      latestDetection = null;
    }
  }, 100);
}

async function captureWebcam() {
  if (!videoRef.value) return;
  
  isLoading.value = true;
  loadingMessage.value = 'Đang phân tích khuôn mặt...';
  errorMessage.value = '';
  
  try {
    if (!latestDetection) {
      errorMessage.value = 'Không tìm thấy khuôn mặt nào. Vui lòng nhìn thẳng vào camera.';
      return;
    }
    
    extractedDescriptor.value = latestDetection.descriptor;
    
    // Create snapshot for preview
    const canvas = document.createElement('canvas');
    canvas.width = videoRef.value.videoWidth;
    canvas.height = videoRef.value.videoHeight;
    canvas.getContext('2d').drawImage(videoRef.value, 0, 0);
    capturedImage.value = canvas.toDataURL('image/jpeg');
    
    stopWebcam();
    mode.value = 'selection';
    showSuccess('Nhận diện khuôn mặt thành công!');
    
  } catch (error) {
    errorMessage.value = 'Lỗi khi phân tích khuôn mặt.';
    console.error(error);
  } finally {
    isLoading.value = false;
  }
}


function saveFaceId() {
  if (!extractedDescriptor.value) return;
  
  const descriptorArray = Array.from(extractedDescriptor.value);
  emit('saved', JSON.stringify(descriptorArray));
  closeModal();
}

function closeModal() {
  stopWebcam();
  mode.value = 'selection';
  extractedDescriptor.value = null;
  capturedImage.value = null;
  errorMessage.value = '';
  emit('close');
}

onUnmounted(() => {
  stopWebcam();
});

</script>

<template>
  <Teleport to="body">
    <div v-if="show" class="fixed inset-0 z-[100] flex items-center justify-center bg-black/60 backdrop-blur-sm">
      <div class="relative w-full max-w-lg rounded-[24px] bg-white p-6 shadow-2xl mx-4 max-h-[90vh] overflow-y-auto">
        <!-- Header -->
        <div class="mb-5 flex items-center justify-between">
          <div>
            <h3 class="text-xl font-bold text-slate-900">Cập nhật Face ID</h3>
            <p class="text-sm text-slate-500 mt-1">
              Nhân viên: <strong class="text-slate-800">{{ employeeName }}</strong>
            </p>
          </div>
          <button type="button" @click="closeModal"
            class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition">
            <X class="h-5 w-5" />
          </button>
        </div>
        
        <!-- Loading -->
        <div v-if="isLoading" class="py-10 text-center space-y-4">
          <div class="inline-flex h-12 w-12 animate-spin items-center justify-center rounded-full border-4 border-slate-200 border-t-primary"></div>
          <p class="text-sm font-medium text-slate-600">{{ loadingMessage }}</p>
        </div>
        
        <!-- Error -->
        <div v-if="errorMessage" class="mb-4 rounded-2xl bg-rose-50 p-4 text-sm font-medium text-rose-600 border border-rose-100 flex items-start gap-2">
          <X class="h-5 w-5 shrink-0" />
          <span>{{ errorMessage }}</span>
        </div>
        
        <!-- Main Content -->
        <div v-if="!isLoading" class="space-y-5">
          
          <!-- Mode: Selection -->
          <div v-if="mode === 'selection'" class="space-y-4">
            <div v-if="hasExistingFaceId" class="mb-4 rounded-2xl bg-emerald-50 border border-emerald-100 p-4 flex items-start gap-3">
               <CheckCircle2 class="h-5 w-5 text-emerald-500 shrink-0 mt-0.5" />
               <div class="text-sm text-emerald-700">
                  <p class="font-bold">Đã có Face ID</p>
                  <p class="mt-0.5 opacity-90">Nhân viên này đã được đăng ký khuôn mặt. Bạn có thể cập nhật lại bằng cách chọn bên dưới.</p>
               </div>
            </div>
            
            <div v-if="capturedImage" class="space-y-3">
              <p class="text-sm font-medium text-slate-700">Khuôn mặt đã lấy được:</p>
              <div class="mx-auto w-40 h-40 rounded-full overflow-hidden border-4 border-primary/20 p-1">
                <img :src="capturedImage" class="w-full h-full object-cover rounded-full" />
              </div>
              <Button @click="saveFaceId" class="w-full justify-center text-base h-12" variant="primary">
                Lưu Face ID này
              </Button>
              <div class="relative flex items-center py-2">
                <div class="flex-grow border-t border-slate-200"></div>
                <span class="mx-4 flex-shrink-0 text-sm text-slate-400">hoặc thử lại</span>
                <div class="flex-grow border-t border-slate-200"></div>
              </div>
            </div>
            
            <div class="grid grid-cols-1 gap-4">
              <button type="button" @click="startWebcam" class="flex flex-col items-center justify-center gap-3 rounded-2xl border-2 border-dashed border-slate-200 p-6 transition hover:border-primary hover:bg-primary/5">
                <div class="rounded-full bg-blue-50 p-4 text-blue-500">
                  <Camera class="h-8 w-8" />
                </div>
                <span class="text-sm font-semibold text-slate-700">Quét khuôn mặt bằng Webcam</span>
              </button>
            </div>
          </div>
          
          <!-- Mode: Webcam -->
          <div v-else-if="mode === 'webcam'" class="space-y-4">
            <div class="relative overflow-hidden rounded-2xl bg-black aspect-video flex items-center justify-center">
              <video ref="videoRef" @play="handleVideoPlay" class="absolute w-full h-full object-cover" autoplay playsinline muted></video>
              <canvas ref="canvasRef" class="absolute inset-0 pointer-events-none"></canvas>
              
              <!-- Scan overlay pattern -->
              <div class="absolute inset-0 border-4 border-white/20 pointer-events-none rounded-2xl">
                 <div class="absolute inset-x-0 h-0.5 bg-green-400/50 shadow-[0_0_8px_rgba(74,222,128,0.8)] animate-pulse" style="top: 50%;"></div>
              </div>
            </div>
            
            <div class="flex gap-3">
              <Button @click="cancelWebcam" variant="outline" class="flex-1 justify-center h-12">
                Hủy
              </Button>
              <Button @click="captureWebcam" variant="primary" class="flex-1 justify-center h-12 gap-2">
                <ScanFace class="h-5 w-5" />
                Chụp khuôn mặt
              </Button>
            </div>
          </div>
          
        </div>
      </div>
    </div>
  </Teleport>
</template>
