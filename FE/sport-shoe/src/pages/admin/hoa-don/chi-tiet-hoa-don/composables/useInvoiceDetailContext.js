import { inject, provide } from "vue";

const invoiceDetailContextKey = Symbol("invoice-detail-context");

export function provideInvoiceDetailContext(context) {
  provide(invoiceDetailContextKey, context);
}

export function useInvoiceDetailContext() {
  const context = inject(invoiceDetailContextKey);
  if (!context) {
    throw new Error("Invoice detail context is missing.");
  }
  return context;
}
