<template>
  <div class="w-full overflow-hidden rounded-[6px] border border-slate-200 bg-white shadow-sm">
    <div class="overflow-x-auto">
      <table class="w-full min-w-[800px] border-collapse text-left text-sm text-slate-600">
        <thead :class="redHeader ? 'bg-primary text-white font-semibold' : 'bg-slate-50 border-b border-slate-200 text-slate-700 font-semibold'">
          <tr>
            <slot name="header"></slot>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <slot name="body"></slot>
        </tbody>
      </table>
    </div>
    <div v-if="$slots.footer" class="border-t border-slate-100 bg-slate-50 px-5 py-3">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  redHeader: Boolean
})
</script>
