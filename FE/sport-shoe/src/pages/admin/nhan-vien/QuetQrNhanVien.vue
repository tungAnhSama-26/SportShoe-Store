<script setup lang="ts">
import { ref } from "vue"
import { useRouter } from "vue-router"
import BanHangQrScannerModal from "../../../components/admin/ban-hang/BanHangQrScannerModal.vue"
import { layDanhSachNhanVien, layNhanVienTheoCccd } from "../../../services/nhan-vien"
import {
  clearEmployeeQrDraft,
  parseEmployeeQrPayload,
  saveEmployeeQrDraft,
} from "../../../utils/employee-qr"

const router = useRouter()
const moQuetQr = ref(true)
const loiTrang = ref("")

function quayLaiDanhSach() {
  moQuetQr.value = false
  clearEmployeeQrDraft()
  router.push({ name: "admin-nhan-vien" })
}

function normalizeMatcher(value: string) {
  return String(value ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, " ")
    .trim()
}

function khopChinhXac(a: string, b: string) {
  const normalizedA = normalizeMatcher(a)
  const normalizedB = normalizeMatcher(b)
  return Boolean(normalizedA) && normalizedA === normalizedB
}

function diaChiGanDung(a: string, b: string) {
  const normalizedA = normalizeMatcher(a)
  const normalizedB = normalizeMatcher(b)
  if (!normalizedA || !normalizedB) return false
  return normalizedA.includes(normalizedB) || normalizedB.includes(normalizedA)
}

async function timNhanVienDaTonTai(payload: Record<string, any>) {
  if (payload.cccd) {
    try {
      return await layNhanVienTheoCccd(payload.cccd)
    } catch (error: any) {
      if (error?.status !== 404) {
        throw error
      }
    }
  }

  const hoTen = String(payload.hoTen ?? "").trim()
  if (!hoTen) return null

  const danhSach = await layDanhSachNhanVien({ keyword: hoTen })
  const trungTen = Array.isArray(danhSach)
    ? danhSach.filter((nhanVien: any) => khopChinhXac(nhanVien.hoTen, hoTen))
    : []

  if (trungTen.length === 1) {
    return trungTen[0]
  }

  const trungTenVaNgaySinh = trungTen.filter(
    (nhanVien: any) =>
      payload.ngaySinh &&
      nhanVien.ngaySinh &&
      String(nhanVien.ngaySinh).trim() === String(payload.ngaySinh).trim(),
  )
  if (trungTenVaNgaySinh.length === 1) {
    return trungTenVaNgaySinh[0]
  }

  const trungTenVaDiaChi = trungTen.filter((nhanVien: any) =>
    diaChiGanDung(nhanVien.diaChi, payload.diaChiCuThe),
  )
  if (trungTenVaDiaChi.length === 1) {
    return trungTenVaDiaChi[0]
  }

  return null
}

async function xuLyDuLieuQr(rawValue: string) {
  loiTrang.value = ""

  try {
    const payload = parseEmployeeQrPayload(rawValue)
    const nhanVien = await timNhanVienDaTonTai(payload)

    if (nhanVien?.id) {
      moQuetQr.value = false
      saveEmployeeQrDraft(payload)
      router.push({
        name: "admin-nhan-vien-chi-tiet",
        params: { id: nhanVien.id },
        query: { fromQr: "1" },
      })
      return
    }

    moQuetQr.value = false
    saveEmployeeQrDraft(payload)
    router.push({ name: "admin-nhan-vien-them", query: { fromQr: "1" } })
  } catch (error) {
    loiTrang.value =
      error instanceof Error ? error.message : "Khong the doc du lieu QR nhan vien."
  }
}
</script>

<template>
  <div>
    <BanHangQrScannerModal
      :open="moQuetQr"
      chip-label="Quet QR nhan vien"
      title="Dung camera de nhan du lieu nhan vien"
      loading-text="Dang bat camera de quet ma QR nhan vien..."
      fallback-helper-text="Dua ma QR nhan vien vao giua khung quet de tu dong dien bieu mau."
      :show-manual-section="false"
      :close-on-scan="false"
      :force-compatibility-scanner="true"
      :scan-formats="['qr_code', 'pdf417']"
      :external-error="loiTrang"
      :show-retry-button="false"
      :show-header-content="false"
      :show-camera-hint="false"
      retry-button-label="Quet lai"
      camera-hint="Uu tien camera sau de quet ma QR nhan vien"
      @close="quayLaiDanhSach"
      @scan="xuLyDuLieuQr"
    />
  </div>
</template>
