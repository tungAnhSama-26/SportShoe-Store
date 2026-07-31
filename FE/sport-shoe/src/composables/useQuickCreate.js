import { computed, reactive, ref } from 'vue'
import * as api from '../services/san-pham-api.js'
import {
  chatLieuGiayApi,
  coGiayApi,
  congNgheDemApi,
  deGiayApi,
  loaiGiayApi,
  mauSacApi,
  thuongHieuApi,
  trongLuongApi,
} from '../services/danh-muc-api.js'
import { hasSpecialCharacters } from '../utils/thuoc-tinh-san-pham.js'

export function useQuickCreate() {
  const quickCreateOpen = ref(false)
  const quickCreateType = ref(null)
  const quickCreateSaving = ref(false)

  const quickCreateForm = reactive({
    ma: '',
    ten: '',
    xuatXu: '',
    hex: '#000000',
  })

  const quickCreateErrors = reactive({})

  const quickCreateDefinitions = {
    thuongHieu: {
      title: 'Thêm nhanh thương hiệu',
      description: 'Tạo thương hiệu mới và gán luôn cho sản phẩm hiện tại.',
      fields: [
        { key: 'ma', label: 'Mã *', placeholder: 'VD: NIKE', uppercase: true },
        {
          key: 'ten',
          label: 'Tên thương hiệu *',
          placeholder: 'Nhập tên thương hiệu...',
        },
        { key: 'xuatXu', label: 'Xuất xứ', placeholder: 'VD: Việt Nam' },
      ],
    },
    loaiGiay: {
      title: 'Thêm nhanh loại giày',
      description: 'Tạo loại giày mới mà không cần rời khỏi form.',
      fields: [
        { key: 'ma', label: 'Mã *', placeholder: 'VD: RUN', uppercase: true },
        {
          key: 'ten',
          label: 'Tên loại giày *',
          placeholder: 'Nhập tên loại giày...',
        },
      ],
    },
    chatLieuGiay: {
      title: 'Thêm nhanh chất liệu',
      description: 'Tạo chất liệu mới và chọn ngay cho sản phẩm.',
      fields: [
        { key: 'ma', label: 'Mã *', placeholder: 'VD: CLMESH', uppercase: true },
        {
          key: 'ten',
          label: 'Tên chất liệu *',
          placeholder: 'Nhập tên chất liệu...',
        },
      ],
    },
    deGiay: {
      title: 'Thêm nhanh đế giày',
      description: 'Tạo đế giày mới ngay trong form sản phẩm.',
      fields: [
        { key: 'ma', label: 'Mã *', placeholder: 'VD: DG01', uppercase: true },
        {
          key: 'ten',
          label: 'Tên đế giày *',
          placeholder: 'Nhập tên đế giày...',
        },
      ],
    },
    coGiay: {
      title: 'Thêm nhanh cổ giày',
      description: 'Tạo cổ giày mới và áp dụng luôn cho sản phẩm.',
      fields: [
        { key: 'ma', label: 'Mã *', placeholder: 'VD: CG01', uppercase: true },
        {
          key: 'ten',
          label: 'Tên cổ giày *',
          placeholder: 'Nhập tên cổ giày...',
        },
      ],
    },
    congNgheDem: {
      title: 'Thêm nhanh công nghệ đế',
      description: 'Tạo công nghệ đế mới cho sản phẩm.',
      fields: [
        { key: 'ma', label: 'Mã *', placeholder: 'VD: AIR', uppercase: true },
        {
          key: 'ten',
          label: 'Tên công nghệ *',
          placeholder: 'Nhập tên công nghệ đế...',
        },
      ],
    },
    trongLuong: {
      title: 'Thêm nhanh trọng lượng',
      description: 'Tạo trọng lượng mới cho sản phẩm.',
      fields: [
        { key: 'ma', label: 'Mã *', placeholder: 'VD: LIGHT', uppercase: true },
        {
          key: 'ten',
          label: 'Tên trọng lượng *',
          placeholder: 'Nhập tên trọng lượng...',
        },
      ],
    },
    mauSac: {
      title: 'Thêm nhanh màu sắc',
      description: 'Tạo màu sắc mới với mã hex.',
      fields: [
        { key: 'ma', label: 'Mã *', placeholder: 'VD: RED', uppercase: true },
        {
          key: 'ten',
          label: 'Tên màu *',
          placeholder: 'Nhập tên màu...',
        },
        { key: 'hex', label: 'Mã màu', type: 'color' },
      ],
    },
  }

  const quickCreateDefinition = computed(() =>
    quickCreateType.value ? quickCreateDefinitions[quickCreateType.value] : null
  )

  function resetQuickCreateForm() {
    Object.assign(quickCreateForm, {
      ma: '',
      ten: '',
      xuatXu: '',
      hex: '#000000',
    })
    Object.assign(quickCreateErrors, {})
  }

  function openQuickCreate(type, preset = '') {
    quickCreateType.value = type
    quickCreateForm.ten = preset
    quickCreateOpen.value = true
  }

  function closeQuickCreate() {
    quickCreateOpen.value = false
    quickCreateType.value = null
    quickCreateSaving.value = false
    resetQuickCreateForm()
  }

  async function handleQuickCreateSave(productForm, danhMuc) {
    quickCreateErrors.value = {}

    // Validation
    if (!quickCreateForm.ma.trim()) {
      quickCreateErrors.value.ma = 'Mã không được để trống'
      return
    } else if (hasSpecialCharacters(quickCreateForm.ma)) {
      quickCreateErrors.value.ma = 'Mã không được chứa ký tự đặc biệt'
      return
    }

    const trimmedTen = quickCreateForm.ten.trim()
    if (!quickCreateForm.ten || !trimmedTen) {
      quickCreateErrors.value.ten = 'Tên không được để trống'
      return
    } else if (quickCreateForm.ten !== trimmedTen) {
      quickCreateErrors.value.ten = 'Tên không được chứa khoảng trắng ở đầu hoặc cuối'
      return
    } else if (trimmedTen.length < 4 || trimmedTen.length > 100) {
      quickCreateErrors.value.ten = 'Tên phải từ 4 đến 100 ký tự'
      return
    } else if (hasSpecialCharacters(trimmedTen)) {
      quickCreateErrors.value.ten = 'Tên không được chứa ký tự đặc biệt'
      return
    }

    if (quickCreateForm.xuatXu && hasSpecialCharacters(quickCreateForm.xuatXu)) {
      quickCreateErrors.value.xuatXu = 'Xuất xứ không được chứa ký tự đặc biệt'
      return
    }

    if (quickCreateType.value === 'mauSac' && !quickCreateForm.hex) {
      quickCreateErrors.value.hex = 'Mã màu không được để trống'
      return
    }

    try {
      quickCreateSaving.value = true

      let result
      switch (quickCreateType.value) {
        case 'thuongHieu':
          result = await thuongHieuApi.create({
            ma: quickCreateForm.ma,
            ten: quickCreateForm.ten,
            xuatXu: quickCreateForm.xuatXu,
          })
          danhMuc.thuongHieu.push(result.data)
          productForm.thuongHieuId = result.data.id
          break

        case 'loaiGiay':
          result = await loaiGiayApi.create({
            ma: quickCreateForm.ma,
            ten: quickCreateForm.ten,
          })
          danhMuc.loaiGiay.push(result.data)
          productForm.loaiGiayId = result.data.id
          break

        case 'chatLieuGiay':
          result = await chatLieuGiayApi.create({
            ma: quickCreateForm.ma,
            ten: quickCreateForm.ten,
          })
          danhMuc.chatLieuGiay.push(result.data)
          productForm.chatLieuGiayId = result.data.id
          break

        case 'deGiay':
          result = await deGiayApi.create({
            ma: quickCreateForm.ma,
            ten: quickCreateForm.ten,
          })
          danhMuc.deGiay.push(result.data)
          productForm.deGiayId = result.data.id
          break

        case 'coGiay':
          result = await coGiayApi.create({
            ma: quickCreateForm.ma,
            ten: quickCreateForm.ten,
          })
          danhMuc.coGiay.push(result.data)
          productForm.coGiayId = result.data.id
          break

        case 'congNgheDem':
          result = await congNgheDemApi.create({
            ma: quickCreateForm.ma,
            ten: quickCreateForm.ten,
          })
          danhMuc.congNgheDem.push(result.data)
          productForm.congNgheDemId = result.data.id
          break

        case 'trongLuong':
          result = await trongLuongApi.create({
            ma: quickCreateForm.ma,
            ten: quickCreateForm.ten,
          })
          danhMuc.trongLuong.push(result.data)
          productForm.trongLuongId = result.data.id
          break

        case 'mauSac':
          result = await mauSacApi.create({
            ma: quickCreateForm.ma,
            ten: quickCreateForm.ten,
            hex: quickCreateForm.hex,
          })
          danhMuc.mauSac.push(result.data)
          break
      }

      closeQuickCreate()
    } catch (error) {
      console.error('Error creating quick item:', error)
      if (error.response?.data?.errors) {
        Object.assign(quickCreateErrors, error.response.data.errors)
      } else {
        quickCreateErrors.value.general = 'Có lỗi xảy ra khi tạo mới'
      }
    } finally {
      quickCreateSaving.value = false
    }
  }

  function updateQuickCreateForm(form) {
    Object.assign(quickCreateForm, form)
  }

  return {
    // Reactive data
    quickCreateOpen,
    quickCreateType,
    quickCreateSaving,
    quickCreateForm,
    quickCreateErrors,

    // Computed
    quickCreateDefinition,

    // Methods
    resetQuickCreateForm,
    openQuickCreate,
    closeQuickCreate,
    handleQuickCreateSave,
    updateQuickCreateForm,
  }
}
