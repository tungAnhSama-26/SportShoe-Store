function useCuonMuot() {
  function diToi(id) {
    document.querySelector(id)?.scrollIntoView({ behavior: "smooth" });
  }
  return { diToi };
}
export {
  useCuonMuot
};
