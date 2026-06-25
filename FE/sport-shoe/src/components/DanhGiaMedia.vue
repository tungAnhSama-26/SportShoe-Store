<script setup>
import { computed, ref } from 'vue';
import { parseMedia, resolveMediaUrl } from '../utils/media';

const props = defineProps({
  media: { type: [String, Array], default: '' },
  size: { type: String, default: 'h-24 w-24' },
});

const danhSach = computed(() =>
  parseMedia(props.media).map((m) => ({ ...m, full: resolveMediaUrl(m.url) })),
);

const anhXem = ref(null); // ảnh đang phóng to trong lightbox
</script>

<template>
  <div v-if="danhSach.length" class="mt-2 flex flex-wrap gap-2">
    <template v-for="(m, i) in danhSach" :key="i">
      <video
        v-if="m.loai === 'video'"
        :src="m.full"
        controls
        :class="size"
        class="rounded-lg bg-slate-100 object-cover"
      />
      <button
        v-else
        type="button"
        @click="anhXem = m.full"
        :class="size"
        class="overflow-hidden rounded-lg bg-slate-100"
      >
        <img :src="m.full" alt="Ảnh đánh giá" class="h-full w-full object-cover" />
      </button>
    </template>
  </div>

  <!-- Lightbox xem ảnh phóng to -->
  <Teleport to="body">
    <div
      v-if="anhXem"
      @click="anhXem = null"
      class="fixed inset-0 z-[200] flex items-center justify-center bg-black/80 p-4"
    >
      <img :src="anhXem" alt="" class="max-h-[90vh] max-w-[90vw] rounded-lg object-contain" />
    </div>
  </Teleport>
</template>
