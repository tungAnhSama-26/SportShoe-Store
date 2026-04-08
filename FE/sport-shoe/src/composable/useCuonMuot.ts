export function useCuonMuot() {
  function diToi(id: string) {
    document.querySelector(id)?.scrollIntoView({ behavior: "smooth" });
  }

  return { diToi };
}
