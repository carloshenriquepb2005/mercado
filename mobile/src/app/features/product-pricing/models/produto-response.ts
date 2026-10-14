export interface ProdutoResponse {
  id: number;
  codigoBarras: string;
  descricao: string;
  unidade: string;
  precoVenda: number;
  codigoNcm: string;
  estoqueAtual: number | null;
}

